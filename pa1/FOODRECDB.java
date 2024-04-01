package ceng.ceng351.foodrecdb;


import com.mysql.cj.protocol.Resultset;

import java.sql.*;
import java.util.Arrays;
import java.util.Vector;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class FOODRECDB implements IFOODRECDB{
    private static String user = "e2448538"; // TODO: Your userName
    private static String password = "bS4HZRV-Vu42-j17"; //  TODO: Your password
    private static String host = "momcorp.ceng.metu.edu.tr"; // host name
    private static String database = "db2448538"; // TODO: Your database name
    private static int port = 8080; // port

    private static Connection con = null;
    @Override
    public void initialize() {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(url, user, password);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return;
    }

    @Override
    public int createTables() {
        int table_number = 0;
        int result;
        Vector<String> create_table = new Vector<>();

        //Player(number:integer, teamname:char(20), name:char(30), age:integer, position:char(3))
        String sql1  = "create table if not exists menu_item (" +
                "itemID int ," +
                "itemName varchar(40) ," +
                "cuisine varchar(20) ," +
                "price int ," +
                "primary key (itemID));";

        String sql2  = "create table if not exists ingredient (" +
                "ingredientID int ," +
                "ingredientName varchar(40) ," +
                "primary key (ingredientID));";

        String sql3  = "create table includes (" +
                "itemID int," +
                "ingredientID int ," +
                "primary key (itemID,ingredientID)," +
                "FOREIGN KEY (itemID) REFERENCES MenuItem(itemID)," +
                "FOREIGN KEY (ingredientID) REFERENCES Ingredient(ingredientID) " +
                "on delete cascade);";

        String sql4 = "create table if not exists ratings (" +
                "ratingID int," +
                "itemID int," +
                "rating int," +
                "ratingDate date," +
                "primary key (ratingID)," +
                "FOREIGN KEY (itemID) REFERENCES MenuItem(itemID));" ;

        String sql5  = "create table if not exists dietary_category (" +
                "ingredientID int ," +
                "dietaryCategory varchar(20) ," +
                "primary key (ingredientID,dietaryCategory)," +
                "FOREIGN KEY (ingredientID) REFERENCES Ingredient(ingredientID) on delete cascade on update cascade);";

        create_table.add(sql1);
        create_table.add(sql2);
        create_table.add(sql3);
        create_table.add(sql4);
        create_table.add(sql5);

        for(int i=0; i < 5; i++) {
            try {
                Statement statement = this.con.createStatement();
                result = statement.executeUpdate(create_table.get(i));
                table_number++;
                statement.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
      //  System.out.println("Created " + table_number +" tables.");
        return table_number;
    }

    @Override
    public int dropTables() {
        int result;
        int dropped_table_number = 0;
        Vector<String> drop_table = new Vector<>();
        String sql5  = "drop table if exists menu_item ";
        String sql1  = "drop table if exists includes";

        String sql2  = "drop table if exists dietary_category";

        String sql3  = "drop table if exists ingredient";

        String sql4 = "drop table if exists ratings";


        drop_table.add(sql1);
        drop_table.add(sql2);
        drop_table.add(sql3);
        drop_table.add(sql4);
        drop_table.add(sql5);

        for(int i=4; i>=0; i--) {
            try {
                Statement statement = this.con.createStatement();
                result = statement.executeUpdate(drop_table.get(i));
                dropped_table_number++;
                statement.close();

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
      //  System.out.println("Dropped " + dropped_table_number +" tables.");
        return dropped_table_number;
    }

    @Override
    public int insertMenuItems(MenuItem[] items) {
        int numberOfRowsInserted = 0;
        int result;

        for(int i=0; i< items.length; i++){

            String query = "insert into menu_item values(\"" +
                    items[i].getItemID() + "\",\"" +
                    items[i].getItemName() + "\",\"" +
                    items[i].getCuisine() + "\",\"" +
                    items[i].getPrice() + "\")";


            try {
                Statement st = con.createStatement();
                result = st.executeUpdate(query);
                System.out.println(result);
                numberOfRowsInserted++;
                st.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
      //  System.out.println("Inserted " + numberOfRowsInserted +" menu_items.");
        return numberOfRowsInserted;    }

    @Override
    public int insertIngredients(Ingredient[] ingredients) {
        int numberOfRowsInserted = 0;
        int result;

        for(int i=0; i< ingredients.length; i++){

            String query = "insert into ingredient values(\"" +
                    ingredients[i].getIngredientID() + "\",\"" +
                    ingredients[i].getIngredientName() + "\")";

            try {
                Statement st = con.createStatement();
                result = st.executeUpdate(query);
                System.out.println(result);
                numberOfRowsInserted++;
                st.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
     //   System.out.println("Inserted " + numberOfRowsInserted +" ingredients.");
        return numberOfRowsInserted;

    }

    @Override
    public int insertIncludes(Includes[] includes) {
        int numberOfRowsInserted = 0;
        int result;

        for(int i=0; i< includes.length; i++){

            String query = "insert into includes values(\"" +
                    includes[i].getItemID() + "\",\"" +
                    includes[i].getIngredientID() + "\")";

            try {
                Statement st = con.createStatement();
                result = st.executeUpdate(query);
                System.out.println(result);
                numberOfRowsInserted++;
                st.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    //    System.out.println("Inserted " + numberOfRowsInserted +" includes.");
        return numberOfRowsInserted;
    }

    @Override
    public int insertDietaryCategories(DietaryCategory[] categories) {
        int numberOfRowsInserted = 0;
        int result;

        for(int i=0; i< categories.length; i++){

            String query = "insert into dietary_category values(\"" +
                    categories[i].getIngredientID() + "\",\"" +
                    categories[i].getDietaryCategory() + "\")";

            try {
                Statement st = con.createStatement();
                result = st.executeUpdate(query);
                System.out.println(result);
                numberOfRowsInserted++;
                st.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    //    System.out.println("Inserted " + numberOfRowsInserted +" categories.");
        return numberOfRowsInserted;

    }

    @Override
    public int insertRatings(Rating[] ratings) {
        int numberOfRowsInserted = 0;
        int result;

        for(int i=0; i< ratings.length; i++){

            String query = "insert into ratings values(\"" +
                    ratings[i].getRatingID() + "\",\"" +
                    ratings[i].getItemID() + "\",\"" +
                    ratings[i].getRating() + "\",\"" +
                    ratings[i].getRatingDate() + "\")";

            try {
                Statement st = con.createStatement();
                result = st.executeUpdate(query);
                System.out.println(result);
                numberOfRowsInserted++;
                st.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    //    System.out.println("Inserted " + numberOfRowsInserted +" ratings.");
        return numberOfRowsInserted;




    }

    @Override
    public MenuItem[] getMenuItemsWithGivenIngredient(String MenuItem) {
        ArrayList<MenuItem> resultList = new ArrayList<MenuItem>();
        MenuItem item;
        MenuItem items[];
        ResultSet queryResult;

        String query = "Select M.itemID, M.itemName, M.cuisine, M.price " +
                "From menu_item M , includes I , ingredient IG " +
                "Where IG.ingredientName = '"+MenuItem+"' " +
                "AND IG.ingredientID = I.ingredientID " +
                "AND M.itemID = I.itemID " +
                "Order By M.itemID asc ;";

        try {
            Statement statement = con.createStatement();
            queryResult = statement.executeQuery(query);

            while( queryResult.next()) {

                int itemID= queryResult.getInt("itemID");
                String itemName = queryResult.getString("itemName");
                String cuisine = queryResult.getString("cuisine");
                int price = queryResult.getInt("price");
                item = new MenuItem(itemID, itemName, cuisine,price);
                resultList.add(item) ;

            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        items = new MenuItem[resultList.size()];
        items = resultList.toArray(items);
    //    System.out.println("3- "+ Arrays.toString(items));
        return items;
    }

    @Override
    public MenuItem[] getMenuItemsWithoutAnyIngredient() {
        ArrayList<MenuItem> resultList = new ArrayList<MenuItem>();
        MenuItem item;
        MenuItem items[];
        ResultSet queryResult;

       String query = "Select  M.itemID, M.itemName, M.cuisine, M.price "+
                "From menu_item M "+
                "Where M.itemID not in ("+
                "Select  I.itemID "+
                "From ingredient IG , includes I "+
                "Where IG.ingredientID = I.ingredientID)"+
                "Order By M.itemID asc";
        try {
            Statement statement = con.createStatement();
            queryResult = statement.executeQuery(query);

            while( queryResult.next()) {

                int itemID= queryResult.getInt("itemID");
                String itemName = queryResult.getString("itemName");
                String cuisine = queryResult.getString("cuisine");
                int price = queryResult.getInt("price");
                item = new MenuItem(itemID, itemName, cuisine,price);
                resultList.add(item) ;

            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        items = new MenuItem[resultList.size()];
        items = resultList.toArray(items);
    //    System.out.println("4- "+ Arrays.toString(items));
        return items;
    }

    @Override
    public Ingredient[] getNotIncludedIngredients() {
        ArrayList<Ingredient> resultList = new ArrayList<Ingredient>();
        Ingredient item;
        Ingredient items[];
        ResultSet queryResult;

        String query = "Select  IG.ingredientID, IG.ingredientName "+
                "From ingredient IG "+
                "Where IG.ingredientID not in ("+
                "Select  I.ingredientID "+
                "From menu_item M , includes I "+
                "Where M.itemID = I.itemID )"+
                "Order By IG.ingredientID asc";

        try {
            Statement statement = con.createStatement();
            queryResult = statement.executeQuery(query);

            while( queryResult.next()) {

                int ingredientID= queryResult.getInt("ingredientID");
                String ingredientName = queryResult.getString("ingredientName");
                item = new Ingredient(ingredientID, ingredientName);
                resultList.add(item) ;

            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        items = new Ingredient[resultList.size()];
        items = resultList.toArray(items);
    //    System.out.println("5- "+ Arrays.toString(items));
        return items;
    }

    @Override
    public MenuItem getMenuItemWithMostIngredients() {
        MenuItem r =null;
        ResultSet rs;
        String get_query = "Select M.itemID, M.itemName, M.cuisine, M.price " +
                "From  menu_item M, (Select M.itemID, COUNT(*) as counts " +
                        "From menu_item M, includes I " +
                        "Where M.itemID = I.itemID " +
                        "Group By M.itemID) as items " +
                "Where M.itemID = items.itemID AND " +
                "items.counts >= (Select (MAX(items.counts)) From (Select M.itemID, COUNT(*) as counts " +
                "From menu_item M, includes I " +
                "Where M.itemID = I.itemID " +
                "Group By M.itemID) as items); " ;
        try {
            Statement st = con.createStatement();
            rs = st.executeQuery(get_query);

            rs.next();

            int itemID= rs.getInt("itemID");
            String itemName = rs.getString("itemName");
            String cuisine = rs.getString("cuisine");
            int price= rs.getInt("price");

            r = new MenuItem(itemID, itemName, cuisine,price);

            //Close
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    //    System.out.println("6- "+ r);
        return  r;
    }

    @Override
    public QueryResult.MenuItemAverageRatingResult[] getMenuItemsWithAvgRatings() {
        Vector<QueryResult.MenuItemAverageRatingResult> vectorResult = new Vector(0);
        QueryResult.MenuItemAverageRatingResult[] result;
        ResultSet queryResult;

        String query = "Select M.itemID , M.itemName, AVG(R.rating) as avgRating " +
                "From menu_item as M LEFT OUTER JOIN ratings as R using (itemID) " +
                "Group By M.itemID " +
                "Order By avgRating desc; ";

        try {
            Statement st = this.con.createStatement();
            queryResult = st.executeQuery(query);
            while(queryResult.next()) {
                String itemID = queryResult.getString("itemID");
                String itemName = queryResult.getString("itemName");
                String avgRating = queryResult.getString("avgRating");
                vectorResult.addElement(new QueryResult.MenuItemAverageRatingResult(itemID, itemName, avgRating));
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        result = vectorResult.toArray(new QueryResult.MenuItemAverageRatingResult[vectorResult.size()]);
    //    System.out.println("7- "+ Arrays.toString(result));
        return result;

    }

    @Override
    public MenuItem[] getMenuItemsForDietaryCategory(String category) {
        ArrayList<MenuItem> resultList = new ArrayList<MenuItem>();
        MenuItem item;
        MenuItem items[];
        ResultSet queryResult;



       String query = "Select m1.itemID, m1.itemName, m1.cuisine, m1.price "+
                "From " +
                        "( Select M1.itemID, COUNT(*) as total_count , M1.itemName, M1.cuisine , M1.price " +
                        "From menu_item  M1, includes  I1 " +
                        "Where M1.itemID = I1.itemID " +
                        "Group BY M1.itemID ) as m1 , " +

                        "(Select M2.itemID, COUNT(*) as ing_count " +
                        "From  dietary_category  D2 , includes  I2, menu_item M2   " +
                        "Where D2.dietaryCategory = '"+category+"' " +
                        "AND D2.ingredientID = I2.ingredientID " +
                        "AND M2.itemID = I2.itemID " +
                        "Group By I2.itemID ) as m2 " +
               "Where m1.itemID = m2.itemID " +
               "AND m1.total_count = m2.ing_count " +
               "Order By m1.itemID asc;";

        try {
            Statement statement = con.createStatement();
            queryResult = statement.executeQuery(query);

            while( queryResult.next()) {

                int itemID= queryResult.getInt("itemID");
                String itemName = queryResult.getString("itemName");
                String cuisine = queryResult.getString("cuisine");
                int price = queryResult.getInt("price");
                item = new MenuItem(itemID, itemName, cuisine,price);
                resultList.add(item) ;

            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        items = new MenuItem[resultList.size()];
        items = resultList.toArray(items);
    //    System.out.println("8- "+ Arrays.toString(items));
        return items;
    }

    @Override
    public Ingredient getMostUsedIngredient() {
        Ingredient r =null;
        ResultSet rs;
        String get_query = " Select IG.ingredientID, IG.ingredientName " +
        "From  ingredient IG, (Select I.ingredientID, COUNT(*) as counts " +
                "From menu_item M, includes I " +
                "Where M.itemID = I.itemID " +
                "Group By I.ingredientID) as items " +
                "Where IG.ingredientID = items.ingredientID AND " +
                "items.counts >= (Select (MAX(items.counts)) From (Select I.ingredientID, COUNT(*) as counts " +
                "From menu_item M, includes I " +
                "Where M.itemID = I.itemID " +
                "Group By I.ingredientID) as items);  ";
        try {
            Statement st = con.createStatement();
            rs = st.executeQuery(get_query);

            rs.next();

            int ingredientID= rs.getInt("ingredientID");
            String ingredientName = rs.getString("ingredientName");
            r = new Ingredient(ingredientID , ingredientName);

            //Close
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    //    System.out.println("9- "+ r);
        return  r;
    }

    @Override
    public QueryResult.CuisineWithAverageResult[] getCuisinesWithAvgRating() {
        Vector<QueryResult.CuisineWithAverageResult> vectorResult = new Vector(0);
        QueryResult.CuisineWithAverageResult[] result;
        ResultSet queryResult;

        String query="Select M.cuisine , AVG(R.rating) as average " +
                "From menu_item as M Left Outer Join ratings as R using(itemID) " +
                "Group By M.cuisine " +
                "Order By average desc;";
        try {
            Statement st = this.con.createStatement();
            queryResult = st.executeQuery(query);
            while(queryResult.next()) {
                String cuisineName = queryResult.getString("cuisine");
                String average = queryResult.getString("average");
                vectorResult.addElement(new QueryResult.CuisineWithAverageResult(cuisineName, average));
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        result = vectorResult.toArray(new QueryResult.CuisineWithAverageResult[vectorResult.size()]);
    //    System.out.println("10- "+ Arrays.toString(result));
        return result;

    }

    @Override
    public QueryResult.CuisineWithAverageResult[] getCuisinesWithAvgIngredientCount() {
        Vector<QueryResult.CuisineWithAverageResult> vectorResult = new Vector(0);
        QueryResult.CuisineWithAverageResult[] result;
        ResultSet queryResult;

        String query = "Select M.cuisine , (COUNT( I.ingredientID))/(COUNT(distinct M.itemID)) as average " +
                "From menu_item as M LEFT OUTER JOIN includes as I using (itemID)  " +

                ", (Select M1.itemID , COUNT(*) as s_count " +
                                    "From menu_item M1 " +
                                    "Group By M1.itemID) as included " +
                                    "" +
                "Where M.itemID = included.itemID " +
                "Group By M.cuisine " +
                "Order By average desc; ";


        try {
            Statement st = this.con.createStatement();
            queryResult = st.executeQuery(query);
            while(queryResult.next()) {
                String cuisineName = queryResult.getString("cuisine");
                String average = queryResult.getString("average");
                vectorResult.addElement(new QueryResult.CuisineWithAverageResult(cuisineName, average));
            }
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        result = vectorResult.toArray(new QueryResult.CuisineWithAverageResult[vectorResult.size()]);
    //    System.out.println("11- "+ Arrays.toString(result));
        return result;
    }

    @Override
    public int increasePrice(String ingredientName, String increaseAmount) {
        int numberOfRowsAffected = 0;
        String query = "Update menu_item  " +
                        "Set price = price + "+Integer.parseInt(increaseAmount.trim())+" " +
                        "Where itemID in " +
                        "(Select I.itemID " +
                        "From  includes I , ingredient IG " +
                        "Where IG.ingredientName = '"+ingredientName+"' " +
                        "AND IG.ingredientID = I.ingredientID " +
                        ");";

        try {
            Statement statement = con.createStatement();
            numberOfRowsAffected = statement.executeUpdate(query);
            statement.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    //    System.out.println("12- "+ numberOfRowsAffected);
        return numberOfRowsAffected;
    }

    @Override
    public Rating[] deleteOlderRatings(String date) {
        ArrayList<Rating> resultList = new ArrayList<Rating>();
        Rating item;
        Rating items[];
        ResultSet queryResult;
        String query =  "Select R.ratingID , R.itemID, R.rating, R.ratingDate " +
                        "From ratings R " +
                        "Where R.ratingDate < \""+date+"\" "+
                        "Order By R.ratingID asc;";
        try {
            Statement statement = con.createStatement();
            queryResult = statement.executeQuery(query);

            while( queryResult.next()) {

                int ratingID= queryResult.getInt("ratingID");
                int itemID = queryResult.getInt("itemID");
                int rating = queryResult.getInt("rating");
                String ratingDate = queryResult.getString("ratingDate");
                item = new Rating(ratingID, itemID, rating,ratingDate);
                resultList.add(item) ;

            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        items = new Rating[resultList.size()];
        items = resultList.toArray(items);
    //    System.out.println("13- "+ Arrays.toString(items));

        query = "Delete From ratings  "+
                "Where ratingDate < "+Integer.parseInt(date.trim().replaceAll("-", ""))+"";
        try {
            Statement st = this.con.createStatement();
            st.executeUpdate(query);
            st.close();
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return items;

    }
}
