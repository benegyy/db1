import psycopg2
import uuid
from datetime import datetime

from config import read_config
from messages import *
from admin import Administrator, User

"""
    Splits given command string by spaces and trims each token.
    Returns token list. also handles quoted strings.
"""
def tokenize_command(command):
    tokens = command.split()
    merged_tokens = []
    in_quotes = False
    current_token = []
    for token in tokens:
        if token.startswith('"') and token.endswith('"') and len(token) > 1:
            merged_tokens.append(token)
        elif token.startswith('"'):
            in_quotes = True
            current_token = [token[1:]]
        elif token.endswith('"'):
            current_token.append(token[:-1])
            merged_tokens.append(' '.join(current_token))
            in_quotes = False
        elif in_quotes:
            current_token.append(token)
        else:
            merged_tokens.append(token)
    return merged_tokens

class Mp2Client:
    user = None
    def __init__(self, config_filename):
        self.db_conn_params = read_config(filename=config_filename, section="postgresql")
        self.conn = None
        # TODO: Implement this function(bind guest user)
        try:
            self.connect()
            with self.conn.cursor() as cursor:
                cursor.execute(
                    'INSERT INTO user (user_id, current_query_count, max_query_limit) VALUES (%s, 0, 10000)',
                    (self.user.user_id,)
                )
            self.conn.commit()
        except Exception as e:
            if self.conn:
                self.conn.rollback()
            print(f"Failed to bind guest user: {str(e)}")
        finally:
            if self.conn:
                self.disconnect()
        
    """
        Connects to PostgreSQL database and returns connection object.
    """
    def connect(self):
        self.conn = psycopg2.connect(**self.db_conn_params)
        self.conn.autocommit = False

    """
        Disconnects from PostgreSQL database.
    """
    def disconnect(self):
        self.conn.close()

    """
        Prints list of available commands of the software.
    """
    def help(self):
        print("\n*** Geographic Information System ***")
        print("> help")
        print("> sign_up <admin_id> <password> <level_id>")
        print("> sign_in <admin_id> <password>")
        print("> sign_out")
        print("> show_levels")
        print("> show_my_level")
        print("> change_level <new_level_id>")
        print("> get_statistics <name> [<country_name>]")
        print("> update_religion <country_name> <religion_name1> <religion_name2> <percentage>")
        print("> transfer_city <city_name> <current_country> <new_country>")
        print("> adjust_population <name> [<country_name>] <new_population>")
        print("> quit")

    
    """
        Saves admin with given details.
        - Return type is a tuple, 1st element is a boolean and 2nd element is the response message from messages.py.
        - If the operation is successful, commit changes and return tuple (True, CMD_EXECUTION_SUCCESS).
        - If the admin_id already exists, return tuple (False, USERNAME_EXISTS).
        - If any exception occurs; rollback, do nothing on the database and return tuple (False, CMD_EXECUTION_FAILED).
    """
    def sign_up(self, admin_id, password, level_id):
        try:
            self.connect()
            cursor = self.conn.cursor()

            # Check if admin_id already exists
            cursor.execute('SELECT COUNT(*) FROM administrator WHERE admin_id = %s', (admin_id,))
            if cursor.fetchone()[0] > 0:
                self.conn.rollback()
                cursor.close()
                return False, USERNAME_EXISTS

            # Check if level_id exists
            cursor.execute('SELECT COUNT(*) FROM accesslevel WHERE level_id = %s', (level_id,))
            if cursor.fetchone()[0] == 0:
                self.conn.rollback()
                cursor.close()
                return False, CMD_EXECUTION_FAILED
                # return False, "Access level does not exist."

            # Insert new admin
            cursor.execute(
                'INSERT INTO administrator (admin_id, password, level_id, session_count) VALUES (%s, %s, %s, 0)',
                (admin_id, password, level_id)
            )

            self.conn.commit()
            cursor.close()
            return True, CMD_EXECUTION_SUCCESS

        except Exception as e:
            if self.conn:
                self.conn.rollback()
            # print("DEBUG sign_up error:", e)

            return False, CMD_EXECUTION_FAILED
        finally:
            self.disconnect()

    """
        Retrieves admin information if admin_id and password is correct and admin's session_count < max_parallel_sessions.
        - Return type is a tuple, 1st element is a Administrator object and 2nd element is the response message from messages.py.
        - If admin_id or password is wrong, return tuple (None, USER_SIGNIN_FAILED).
        - If session_count < max_parallel_sessions, commit changes (increment session_count) and return tuple (Administrator, CMD_EXECUTION_SUCCESS).
        - If session_count >= max_parallel_sessions, return tuple (None, USER_ALL_SESSIONS_ARE_USED).
        - If any exception occurs; rollback, do nothing on the database and return tuple (None, USER_SIGNIN_FAILED).
        - Do not forget the remove the guest user.
    """
    def sign_in(self, admin_id, password):
        # Already signed in checks
        if isinstance(self.user, Administrator):
            if self.user.admin_id == admin_id:
                return self.user, USER_ALREADY_SIGNED_IN
            else:
                return None, USER_OTHER_SIGNED_IN

        try:
            self.connect()
            cursor = self.conn.cursor()

            # Verify admin credentials
            cursor.execute(
                "SELECT a.admin_id, a.level_id, a.session_count, l.max_parallel_sessions " +
                "FROM administrator a JOIN accesslevel l ON a.level_id = l.level_id " +
                "WHERE a.admin_id = %s AND a.password = %s",
                (admin_id, password)
            )

            result = cursor.fetchone()
            if not result:
                self.conn.rollback()
                cursor.close()
                return None, USER_SIGNIN_FAILED

            admin_id, level_id, session_count, max_parallel_sessions = result

            if session_count >= max_parallel_sessions:
                self.conn.rollback()
                cursor.close()
                return None, USER_ALL_SESSIONS_ARE_USED

            # Increment session count
            cursor.execute(
                "UPDATE administrator SET session_count = session_count + 1 WHERE admin_id = %s",
                (admin_id,)
            )

            # Remove guest user
            cursor.execute('DELETE FROM "User" WHERE user_id = %s', (self.user.user_id,))

            # Create Administrator object
            admin = Administrator(admin_id=admin_id, session_count=session_count, plan_id=level_id)
            self.conn.commit()
            cursor.close()
            return admin, CMD_EXECUTION_SUCCESS

        except Exception as e:
            if self.conn:
                self.conn.rollback()
            print("DEBUG sign_in error:", e)
            return None, USER_SIGNIN_FAILED
        finally:
            self.disconnect()

    """
        Signs out from given admin's account.
        - Return type is a tuple, 1st element is a boolean and 2nd element is the response message from messages.py.
        - Decrement session_count of the admin in the database.
        - If the operation is successful, commit changes and return tuple (True, CMD_EXECUTION_SUCCESS).
        - If no admin is account is signed in, return tuple (False, NO_ACTIVE_ADMIN).
        - If any exception occurs; rollback, do nothing on the database and return tuple (False, CMD_EXECUTION_FAILED).
        - Do not forget to recreate a guest user.
    """
    def sign_out(self, admin):
        if not admin:
            return False, NO_ACTIVE_ADMIN

        try:
            self.connect()
            cursor = self.conn.cursor()

            # Check current session count before decrement
            cursor.execute(
                "SELECT session_count FROM administrator WHERE admin_id = %s",
                (admin.admin_id,)
            )
            current = cursor.fetchone()[0]
            if current <= 0:
                self.conn.rollback()
                cursor.close()
                return False, NO_ACTIVE_ADMIN

            cursor.execute(
                "UPDATE administrator SET session_count = session_count - 1 WHERE admin_id = %s",
                (admin.admin_id,)
            )

            # Create a new guest user in "User" table
            self.user = User(user_id=str(uuid.uuid4()))
            cursor.execute(
                'INSERT INTO "User" (user_id, current_query_count, max_query_limit) VALUES (%s, 0, 10000)',
                (self.user.user_id,)
            )

            self.conn.commit()
            cursor.close()
            return True, CMD_EXECUTION_SUCCESS

        except Exception as e:
            if self.conn:
                self.conn.rollback()
            # print("DEBUG sign_out error:", e)  # Optional: see what fails
            return False, CMD_EXECUTION_FAILED
        finally:
            self.disconnect()

    """
        Quits from program.
        - Return type is a tuple, 1st element is a boolean and 2nd element is the response message from messages.py.
        - Remember to sign authenticated user/admin out first.
        - If the operation is successful, commit changes and return tuple (True, CMD_EXECUTION_SUCCESS).
        - If any exception occurs; rollback, do nothing on the database and return tuple (False, CMD_EXECUTION_FAILED).
        - If not authenticated, do not forget to remove the guest user.
    """
    def quit(self, admin):
        try:
            self.connect()
            cursor = self.conn.cursor()

            if admin:
                # Decrement session count (must be >= 1, otherwise reject or enforce at DB level)
                cursor.execute(
                    "UPDATE administrator SET session_count = session_count - 1 WHERE admin_id = %s",
                    (admin.admin_id,)
                )
            else:
                # Remove guest user from "User" table
                cursor.execute(
                    'DELETE FROM "User" WHERE user_id = %s',
                    (self.user.user_id,)
                )

            self.conn.commit()
            cursor.close()
            return True, CMD_EXECUTION_SUCCESS

        except Exception as e:
            if self.conn:
                self.conn.rollback()
            print("DEBUG quit error:", e)
            return False, CMD_EXECUTION_FAILED
        finally:
            self.disconnect()


    """
        Retrieves all available access levels and prints them.
        - Return type is a tuple, 1st element is a boolean and 2nd element is the response message from messages.py.
        - If the operation is successful; print available plans and return tuple (True, CMD_EXECUTION_SUCCESS).
        - If any exception occurs; return tuple (False, CMD_EXECUTION_FAILED).
        
        Output should be like:
        #|Name|Max Sessions
        1|Basic|2
        2|Advanced|5
        3|Premium|10
    """
    def show_levels(self):
        try:
            self.connect()
            cursor = self.conn.cursor()

            cursor.execute(
                "SELECT level_id, name, max_parallel_sessions FROM accesslevel ORDER BY level_id"
            )

            levels = cursor.fetchall()

            print(f"{'ID':<5}|{'Level Name':<15}|{'Max Sessions':<15}")
            for level in levels:
                level_id, name, max_sessions = level
                print(f"{str(level_id):<5}|{name:<15}|{str(max_sessions):<15}")

            cursor.close()
            return True, CMD_EXECUTION_SUCCESS

        except Exception as e:
            return False, CMD_EXECUTION_FAILED
        finally:
            self.disconnect()
    
    """
        Retrieves plan of the authenticated admin.
        - Return type is a tuple, 1st element is a boolean and 2nd element is the response message from messages.py.
        - If the operation is successful; print the admin's plan and return tuple (True, CMD_EXECUTION_SUCCESS).
        - If the admin is not signed in, return tuple (False, USER_NOT_AUTHORIZED).
        - If any other exception occurs; return tuple (False, CMD_EXECUTION_FAILED).
        
        Output should be like:
        #|Name|Max Sessions
        1|Basic|2
    """
    def show_my_level(self, admin):
        if not admin:
            return False, USER_NOT_AUTHORIZED

        try:
            self.connect()
            cursor = self.conn.cursor()

            cursor.execute(
                "SELECT l.level_id, l.name, l.max_parallel_sessions " +
                "FROM accesslevel l JOIN administrator a ON l.level_id = a.level_id " +
                "WHERE a.admin_id = %s",
                (admin.admin_id,)
            )

            level = cursor.fetchone()
            if level:
                level_id, name, max_sessions = level
                print(f"{'ID':<5}|{'Level Name':<15}|{'Max Sessions':<15}")
                print(f"{str(level_id):<5}|{name:<15}|{str(max_sessions):<15}")

            cursor.close()
            return True, CMD_EXECUTION_SUCCESS

        except Exception as e:
            return False, CMD_EXECUTION_FAILED
        finally:
            self.disconnect()

    """
        Subscribe authenticated administrator to new plan.
        - Return type is a tuple, 1st element is a Administrator object and 2nd element is the response message from messages.py.
        - If the new_level_id is a downgrade; rollback, do nothing on the database and return tuple (None, DOWNGRADE_NOT_ALLOWED).
        - If the new_level_id is the same level; rollback, do nothing on the database and return tuple (None, SAMEGRADE_NOT_ALLOWED).
        - If the operation is successful, commit changes and return tuple (admin, CMD_EXECUTION_SUCCESS).
        - If any other exception occurs; rollback, do nothing on the database and return tuple (None, CMD_EXECUTION_FAILED).
    """
    def change_level(self, admin, new_level_id):
        if not admin:
            return None, USER_NOT_AUTHORIZED

        try:
            self.connect()
            cursor = self.conn.cursor()

            # Get current level and session count
            cursor.execute(
                "SELECT level_id, session_count FROM administrator WHERE admin_id = %s",
                (admin.admin_id,)
            )
            result = cursor.fetchone()
            if not result:
                self.conn.rollback()
                cursor.close()
                return None, CMD_EXECUTION_FAILED

            current_level_id, current_session_count = result

            # Ensure only one session is active
            if current_session_count > 1:
                self.conn.rollback()
                cursor.close()
                return None, "Please sign out of your other sessions first."
                # cannot find message in messages.py, but specified in pdf like this

            # Check if level exists
            cursor.execute(
                "SELECT max_parallel_sessions FROM accesslevel WHERE level_id = %s",
                (new_level_id,)
            )
            new_level_row = cursor.fetchone()
            if not new_level_row:
                self.conn.rollback()
                cursor.close()
                return None, CMD_EXECUTION_FAILED

            # Check if trying to downgrade or not upgrade
            if int(new_level_id) <= int(current_level_id):
                self.conn.rollback()
                cursor.close()
                return None, SAMEGRADE_NOT_ALLOWED if int(new_level_id) == int(
                    current_level_id) else DOWNGRADE_NOT_ALLOWED

            # Update admin's level
            cursor.execute(
                "UPDATE administrator SET level_id = %s WHERE admin_id = %s",
                (new_level_id, admin.admin_id)
            )

            # Update admin object in memory
            admin.plan_id = new_level_id

            self.conn.commit()
            cursor.close()
            return admin, CMD_EXECUTION_SUCCESS

        except Exception as e:
            if self.conn:
                self.conn.rollback()
            return None, CMD_EXECUTION_FAILED
        finally:
            self.disconnect()

    """
        Retrieves statistics of the given city/country/continent.
        - Return type is a tuple, 1st element is a boolean and 2nd element is the response message from messages.py.
        - If the operation is successful; print the statistics and return tuple (True, CMD_EXECUTION_SUCCESS).
        - If the name is not unique and country_name is not given, return tuple (False, AMBIGUOUS_CITY).
        - If the name is not unique and country_name is given, return tuple (False, NO_ENTITY_FOUND).
        - If any other exception occurs; return tuple (False, CMD_EXECUTION_FAILED).
        - Do not forget the users table.
    """
    def get_statistics(self, name, country_name=None):
        try:
            self.connect()
            cursor = self.conn.cursor()

            # Clean input
            name = name.strip().strip("'\"")
            if country_name:
                country_name = country_name.strip().strip("'\"")

            if country_name:
                # 1. Try city-country combination (using country_code)
                cursor.execute("""
                    SELECT c.name, c.country_code, c.population, c.elevation 
                    FROM city c 
                    JOIN country co ON c.country_code = co.code
                    WHERE c.name = %s AND co.code = %s
                """, (name, country_name))
                city = cursor.fetchone()

                if city:
                    cname, ccode, population, elevation = city
                    print("TYPE | NAME         | COUNTRY | POPULATION | ELEVATION")
                    print(
                        f"City | {cname:<12} | {country_name:<7} | {population or 'None':<10} | {f'{elevation}m' if elevation else 'None'}")
                    cursor.close()
                    return True, CMD_EXECUTION_SUCCESS

                # No match with both, return not found
                cursor.close()
                return False, NO_ENTITY_FOUND

            # If only one argument:
            # 1. Try Continent
            cursor.execute("SELECT name FROM continent WHERE name = %s", (name,))
            if cursor.fetchone():
                cursor.execute("""
                    SELECT COUNT(*) FROM encompasses 
                    WHERE continent_name = %s AND percentage > 50
                """, (name,))
                count = cursor.fetchone()[0]
                print(f"{'TYPE':<10}|{'NAME':<20}|{'COUNTRIES':<10}")
                print(f"{'Continent':<10}|{name:<20}|{count:<10}")
                cursor.close()
                return True, CMD_EXECUTION_SUCCESS

            # 2. Try Country
            cursor.execute("SELECT code, name, population FROM country WHERE name = %s", (name,))
            country = cursor.fetchone()
            if country:
                code, cname, population = country

                # GDP and economic details
                cursor.execute("SELECT gdp, agriculture, industry, service, unemployment FROM economy WHERE country_code = %s", (code,))
                eco = cursor.fetchone()
                if eco:
                    gdp, agri, ind, serv, unemp = eco
                    gdp_str = f"${gdp:,.2f}" if gdp else "None"
                    agri_str = f"{agri:.1f}%" if agri is not None else "None"
                    ind_str = f"{ind:.1f}%" if ind is not None else "None"
                    serv_str = f"{serv:.1f}%" if serv is not None else "None"
                    unemp_str = f"{unemp:.1f}%" if unemp is not None else "None"
                else:
                    gdp_str = agri_str = ind_str = serv_str = unemp_str = "None"

                # Top language
                cursor.execute("SELECT language, percentage FROM spoken WHERE country_code = %s ORDER BY percentage DESC LIMIT 1", (code,))
                lang = cursor.fetchone()
                language_str = f"{lang[0]} ({lang[1]}%)" if lang else "None"

                # Top religion
                cursor.execute("SELECT name, percentage FROM religion WHERE country_code = %s ORDER BY percentage DESC LIMIT 1", (code,))
                religion = cursor.fetchone()
                religion_str = f"{religion[0]} ({religion[1]}%)" if religion else "None"

                print("TYPE    | NAME     | POPULATION | GDP        | AGRICULTURE | INDUSTRY | SERVICE | UNEMPLOYMENT | TOP_LANGUAGE     | TOP_RELIGION")
                print(f"Country | {cname:<8} | {population or 'None':<10} | {gdp_str:<10} | {agri_str:<11} | {ind_str:<8} | {serv_str:<7} | {unemp_str:<13} | {language_str:<16} | {religion_str}")
                cursor.close()
                return True, CMD_EXECUTION_SUCCESS

            # 3. Try City (check for ambiguity)
            cursor.execute("SELECT COUNT(*) FROM city WHERE name = %s", (name,))
            count = cursor.fetchone()[0]
            if count > 1:
                cursor.close()
                return False, AMBIGUOUS_CITY
            elif count == 1:
                cursor.execute("""
                    SELECT name, country_code, population, elevation 
                    FROM city WHERE name = %s
                """, (name,))
                cname, ccode, population, elevation = cursor.fetchone()
                print("TYPE | NAME         | POPULATION | ELEVATION")
                print(f"City | {cname:<12} | {population or 'None':<10} | {f'{elevation}m' if elevation else 'None'}")
                cursor.close()
                return True, CMD_EXECUTION_SUCCESS

            cursor.close()
            return False, NO_ENTITY_FOUND

        except Exception as e:
            print("DEBUG ERROR:", e)
            return False, CMD_EXECUTION_FAILED
        finally:
            self.disconnect()
    """
        Updates the religion of the given country.
        - Return type is a tuple, 1st element is a boolean and 2nd element is the response message from messages.py.
        - If the operation is successful; commit changes and return tuple (True, CMD_EXECUTION_SUCCESS).
        - If the country_name is not found, return tuple (False, NO_ENTITY_FOUND).
        - If the religion_name2 is not found, return tuple (False, RELIGION_NOT_FOUND).
        - If the religion_name2 has insufficient percentage, return tuple (False, RELIGION_INSUFFICIENT_PERCENTAGE).
        - If the percentages would be out of 0-100, return tuple (False, INVALID_PERCENTAGE).
        - If the admin is not signed in, return tuple (False, USER_NOT_AUTHORIZED).
    """
    def update_religion(self, admin, country_name, religion_name1, religion_name2, percentage):
        if not admin:
            return False, USER_NOT_AUTHORIZED

        try:
            self.connect()
            cursor = self.conn.cursor()

            # Get country code
            cursor.execute("SELECT code FROM country WHERE name ILIKE %s", (country_name,))
            row = cursor.fetchone()
            if not row:
                cursor.close()
                return False, NO_ENTITY_FOUND
            country_code = row[0]

            try:
                delta = float(percentage)
                if not (0 <= delta <= 100):
                    cursor.close()
                    return False, INVALID_PERCENTAGE
            except ValueError:
                cursor.close()
                return False, INVALID_PERCENTAGE

            # Get religion2 (must exist)
            cursor.execute("""
                SELECT percentage FROM religion
                WHERE name = %s AND country_code = %s
            """, (religion_name2, country_code))
            r2 = cursor.fetchone()
            if not r2:
                cursor.close()
                return False, RELIGION_NOT_FOUND
            r2_old = float(r2[0])

            #  Check if it has enough to subtract
            if delta > r2_old:
                cursor.close()
                return False, RELIGION_INSUFFICIENT_PERCENTAGE

            # Get religion1 (may or may not exist)
            cursor.execute("""
                SELECT percentage FROM religion
                WHERE name = %s AND country_code = %s
            """, (religion_name1, country_code))
            r1 = cursor.fetchone()
            r1_old = float(r1[0]) if r1 else 0.0
            r1_exists = r1 is not None

            # Compute new values
            r1_new = r1_old + delta
            r2_new = r2_old - delta

            if not (0 <= r1_new <= 100 and 0 <= r2_new <= 100):
                cursor.close()
                return False, INVALID_PERCENTAGE

            # Insert or update religion1
            if r1_exists:
                cursor.execute("""
                    UPDATE religion SET percentage = %s
                    WHERE name = %s AND country_code = %s
                """, (r1_new, religion_name1, country_code))
            else:
                cursor.execute("""
                    INSERT INTO religion (country_code, name, percentage)
                    VALUES (%s, %s, %s)
                """, (country_code, religion_name1, r1_new))

            # Update or delete religion2
            if r2_new == 0:
                cursor.execute("""
                    DELETE FROM religion
                    WHERE name = %s AND country_code = %s
                """, (religion_name2, country_code))
            else:
                cursor.execute("""
                    UPDATE religion SET percentage = %s
                    WHERE name = %s AND country_code = %s
                """, (r2_new, religion_name2, country_code))

            self.conn.commit()
            cursor.close()

            # Output
            print("RELIGION| PERCENTAGE")
            print(f"{religion_name1:<10}| {r1_new:.1f}% (+{delta})")
            print(f"{religion_name2:<10}| {r2_new:.1f}% (-{delta})")
            return True, CMD_EXECUTION_SUCCESS

        except Exception as e:
            if self.conn:
                self.conn.rollback()
            print("DEBUG update_religion error:", e)
            return False, CMD_EXECUTION_FAILED
        finally:
            self.disconnect()


    """
        Transfers the city from current country to new country. follow the instructions in mp2.pdf
        - Return type is a tuple, 1st element is a boolean and 2nd element is the response message from messages.py.
        - If the operation is successful; commit changes and return tuple (True, CMD_EXECUTION_SUCCESS).
        - If the city with given current_country is not found, return tuple (False, NO_ENTITY_FOUND).
        - If the country with given new_country is not found, return tuple (False, MISSING_OCCUPIER_COUNTRY).
        - If both countries are the same, return tuple (False, SAME_COUNTRY).
        - If the admin is not signed in, return tuple (False, USER_NOT_AUTHORIZED).
    """
    def transfer_city(self, admin, city_name, current_country, new_country):
        if not admin:
            return False, USER_NOT_AUTHORIZED

        if not city_name or not current_country or not new_country:
            return False, CMD_INVALID_ARGS

        try:
            self.connect()
            cursor = self.conn.cursor()

            # Get current country code
            cursor.execute("SELECT code FROM country WHERE name ILIKE %s", (current_country,))
            current_country_row = cursor.fetchone()
            if not current_country_row:
                cursor.close()
                return False, NO_ENTITY_FOUND
            current_country_code = current_country_row[0]

            # Get new country code
            cursor.execute("SELECT code FROM country WHERE name ILIKE %s", (new_country,))
            new_country_row = cursor.fetchone()
            if not new_country_row:
                cursor.close()
                return False, MISSING_OCCUPIER_COUNTRY
            new_country_code = new_country_row[0]

            # Ensure city is in the current country
            cursor.execute("""
                SELECT name, population FROM city 
                WHERE name ILIKE %s AND country_code = %s
            """, (city_name, current_country_code))
            city_row = cursor.fetchone()
            if not city_row:
                cursor.close()
                return False, NO_ENTITY_FOUND

            if current_country_code == new_country_code:
                cursor.close()
                return False, SAME_COUNTRY

            city_id, city_pop = city_row
            if city_pop is None:
                city_pop = 0

            # Perform city transfer
            cursor.execute("""
                UPDATE city SET country_code = %s 
                WHERE name = %s AND country_code = %s
            """, (new_country_code, city_id, current_country_code))

            # Update populations
            cursor.execute("""
                UPDATE country SET population = population - %s 
                WHERE code = %s
            """, (city_pop, current_country_code))
            cursor.execute("""
                UPDATE country SET population = population + %s 
                WHERE code = %s
            """, (city_pop, new_country_code))

            # ✅ Check if current country has no more cities
            cursor.execute("""
                SELECT COUNT(*) FROM city WHERE country_code = %s
            """, (current_country_code,))
            remaining = cursor.fetchone()[0]

            if remaining == 0:
                # ✅ Delete dependent rows first
                cursor.execute("DELETE FROM religion WHERE country_code = %s", (current_country_code,))
                cursor.execute("DELETE FROM spoken WHERE country_code = %s", (current_country_code,))
                cursor.execute("DELETE FROM economy WHERE country_code = %s", (current_country_code,))
                cursor.execute("DELETE FROM encompasses WHERE country_code = %s", (current_country_code,))

                # ✅ Then delete the country itself
                cursor.execute("DELETE FROM country WHERE code = %s", (current_country_code,))

            self.conn.commit()
            cursor.close()
            return True, CMD_EXECUTION_SUCCESS

        except Exception as e:
            if self.conn:
                self.conn.rollback()
            print("DEBUG transfer_city error:", e)
            return False, CMD_EXECUTION_FAILED
        finally:
            self.disconnect()

    """
        Adjusts the population of the given city/country. follow the instructions in mp2.pdf
        - Return type is a tuple, 1st element is a boolean and 2nd element is the response message from messages.py.
        - If the operation is successful; commit changes and return tuple (True, CMD_EXECUTION_SUCCESS).
        - If the population is negative, return tuple (False, NO_NEGATIVE_POPULATION).
        - If the city/country with given name is not found, return tuple (False, NO_ENTITY_FOUND).
        - If the city name is not unique, return tuple (False, AMBIGUOUS_CITY).
        - If the admin is not signed in, return tuple (False, USER_NOT_AUTHORIZED).
    """
    def adjust_population(self, admin, name, country_name=None, new_population=None):
        if not admin:
            return False, USER_NOT_AUTHORIZED

        # Handle case where only 3 arguments are passed
        if new_population is None and country_name is not None:
            try:
                new_population = int(country_name)
                country_name = None
            except ValueError:
                pass

        # Ensure population is a valid positive int
        try:
            new_population = int(new_population)
            if new_population <= 0:
                return False, NO_NEGATIVE_POPULATION
        except (ValueError, TypeError):
            return False, CMD_EXECUTION_FAILED

        try:
            self.connect()
            cursor = self.conn.cursor()

            # --- Check if it's a country ---
            if country_name is None:
                cursor.execute("SELECT code FROM country WHERE name = %s", (name,))
                result = cursor.fetchone()
                if result:
                    code = result[0]
                    cursor.execute("UPDATE country SET population = %s WHERE code = %s", (new_population, code))
                    self.conn.commit()
                    cursor.close()
                    return True, CMD_EXECUTION_SUCCESS

            # --- Otherwise, check city ---
            if country_name:
                cursor.execute("SELECT code FROM country WHERE name = %s", (country_name,))
                ctry = cursor.fetchone()
                if not ctry:
                    cursor.close()
                    return False, NO_ENTITY_FOUND
                country_code = ctry[0]

                cursor.execute("SELECT name, population FROM city WHERE name = %s AND country_code = %s",
                               (name, country_code))
            else:
                cursor.execute("SELECT COUNT(*) FROM city WHERE name = %s", (name,))
                count = cursor.fetchone()[0]
                if count > 1:
                    cursor.close()
                    return False, AMBIGUOUS_CITY

                cursor.execute("SELECT name, population, country_code FROM city WHERE name = %s", (name,))

            row = cursor.fetchone()
            if not row:
                cursor.close()
                return False, NO_ENTITY_FOUND

            if country_name:
                city_id, old_pop = row
            else:
                city_id, old_pop, country_code = row

            if old_pop is None:
                old_pop = 0  # Treat null as zero

            pop_diff = new_population - old_pop

            # Update city pop
            # Update city by composite key (name + country_code)
            cursor.execute(
                "UPDATE city SET population = %s WHERE name = %s AND country_code = %s",
                (new_population, name, country_code)
            )

            # Update country pop
            cursor.execute("UPDATE country SET population = population + %s WHERE code = %s", (pop_diff, country_code))

            self.conn.commit()
            cursor.close()
            return True, CMD_EXECUTION_SUCCESS

        except Exception as e:
            if self.conn:
                self.conn.rollback()
            print("DEBUG adjust_population error:", e)
            return False, CMD_EXECUTION_FAILED
        finally:
            self.disconnect()

