package student_pack;

import java.util.ArrayList;

public class PlaylistNodeSecondaryIndex extends PlaylistNode {
	private ArrayList<String> genres;
	private ArrayList<Integer> audioIDs;
	private ArrayList<PlaylistNode> children;

	public PlaylistNodeSecondaryIndex(PlaylistNode parent) {
		super(parent);
		genres = new ArrayList<String>();
		children = new ArrayList<PlaylistNode>();
		this.type = PlaylistNodeType.Internal;
	}
	
	public PlaylistNodeSecondaryIndex(PlaylistNode parent, ArrayList<String> genres, ArrayList<PlaylistNode> children) {
		super(parent);
		this.genres = genres;
		this.children = children;
		this.type = PlaylistNodeType.Internal;
	}
	public PlaylistNodeSecondaryIndex(PlaylistNode parent, ArrayList<String> genres, ArrayList<Integer> keys, ArrayList<PlaylistNode> children)
	{
		super(parent);
		this.genres = genres;
		this.children = children;
		this.type = PlaylistNodeType.Internal;
	}
	
	// GUI Methods - Do not modify
	public ArrayList<PlaylistNode> getAllChildren()
	{
		return this.children;
	}
	
	public PlaylistNode getChildrenAt(Integer index) {
		
		return this.children.get(index);
	}
	

	public Integer genreCount()
	{
		return this.genres.size();
	}
	
	public String genreAtIndex(Integer index) {
		if(index >= this.genreCount() || index < 0) {
			return "Not Valid Index!!!";
		}
		else {
			return this.genres.get(index);
		}
	}
	
	
	// Extra functions if needed
	public Integer keyCount()
	{
		return this.audioIDs.size();
	}
	public Integer keyAtIndex(Integer index)
	{
		if(index >= this.keyCount() || index < 0)
		{
			return -1;
		}
		else
		{
			return this.audioIDs.get(index);
		}
	}

	public void addSong (CengSong song){
		// if its children are nodes
		if (this.children.get(0).type == PlaylistNodeType.Internal){

			Integer length, i=0;
			length = this.genres.size();
			for(i=0;  i<length && this.genres.get(i).compareTo(song.genre())<0 ;i++);
			((PlaylistNodeSecondaryIndex)this.children.get(i)).addSong(song);
		}
		// if its children are leaves


		else{

			ArrayList<ArrayList<CengSong>> songs;
			Integer length, i;
			boolean a=true;
			length = this.genres.size();
			for(i=0;  i<length && this.genres.get(i).compareTo(song.genre())<=0; i++);
			/*for(; i<length && this.genres.get(i)==song.genre() && this.audioIDs.get(i)<song.audioId(); i++);*/
			// if it is not full

			if(((PlaylistNodeSecondaryLeaf)this.children.get(i)).getSongBucket().size() == 2*PlaylistNode.order){
				songs = ((PlaylistNodeSecondaryLeaf) this.children.get(i)).getSongBucket();
				length = songs.size();

				for (int j = 0; j < length ; j++) {
					if(songs.get(j).get(0).genre().compareTo(song.genre()) == 0){
						songs.get(j).add(song);
							a = false;
							return;}

				}
			}
			if (((PlaylistNodeSecondaryLeaf)this.children.get(i)).getSongBucket().size() < 2*PlaylistNode.order){


				if (((PlaylistNodeSecondaryLeaf)this.children.get(i)).getSongBucket().get(i).get(0).genre().compareTo(song.genre())==0){

					((PlaylistNodeSecondaryLeaf) this.children.get(i)).getSongBucket().get(i).add(song);
					a = false;

				}
				else {


					Integer j;
					songs = ((PlaylistNodeSecondaryLeaf) this.children.get(i)).getSongBucket();
					length = songs.size();
					for (j = 0; j < length && songs.get(j).get(0).genre().compareTo(song.genre()) < 0; j++) ;
					((PlaylistNodeSecondaryLeaf) this.children.get(i)).addSong(j, song);
				}
			}
			// if it is full
					/*
		*
				for (int j = 0; j < length && songs.get(j).get(0).genre().compareTo(song.genre()) < 0; j++) {
					if (songs.get(j).get(0).genre().compareTo(song.genre()) == 0) {
						songs.get(j).add(song);
						a = false;
						break;
					}
				}
				if(a){
				for (int j = 0; j < length && songs.get(j).get(0).genre().compareTo(song.genre()) < 0; j++) {
						for (i = 0; i < length && songs.get(i).get(0).genre().compareTo(song.genre()) < 0; i++);
						songs.get(i).add(song);
						nodeGenre = songs.get(i).get(PlaylistNode.order).genre();
						genres = new ArrayList<String>();
						genres.add(nodeGenre);
						children = new ArrayList<PlaylistNode>();
						new_leaf1 = new PlaylistNodeSecondaryLeaf(null, (new ArrayList<ArrayList<CengSong>>(songs.subList(0, PlaylistNode.order))));
						new_leaf2 = new PlaylistNodeSecondaryLeaf(null, (new ArrayList<ArrayList<CengSong>>(songs.subList(PlaylistNode.order, 2 * PlaylistNode.order + 1))));

						children.add(new_leaf1);
						children.add(new_leaf2);

						secondaryRoot = new PlaylistNodeSecondaryIndex(null, genres, children);
						new_leaf1.setParent(secondaryRoot);
						new_leaf2.setParent(secondaryRoot);
					}
				}
		* */
			else{


				PlaylistNodeSecondaryLeaf newChild1, newChild2, currentLeaf;
				String	nodeGenre;
				int j;
				currentLeaf = (PlaylistNodeSecondaryLeaf)this.children.get(i);
				songs = currentLeaf.getSongBucket();
				length = songs.size();
				boolean aa= true;


				for ( j = 0; j < length && songs.get(j).get(0).genre().compareTo(song.genre()) < 0; j++);

				if (songs.get(j).get(0).genre().compareTo(song.genre()) == 0) {

						songs.get(j).add(song);
						a = false;

					}

				if(aa){


					//for ( j = 0; j < length && songs.get(j).get(0).genre().compareTo(song.genre()) < 0; j++) {
						//for (i = 0; i < length && songs.get(i).get(0).genre().compareTo(song.genre()) < 0; i++);
						//songs.get(i).add(song);
					ArrayList<CengSong> new_genre = new ArrayList<CengSong>();
					new_genre.add(song);
					nodeGenre = songs.get(j).get(0).genre();
					songs.add(j,new_genre);
						//nodeGenre = songs.get(j).get(0).genre();
						//genres = new ArrayList<String>();
						//genres.add(nodeGenre);
						//children = new ArrayList<PlaylistNode>();

						newChild1 = new PlaylistNodeSecondaryLeaf(null, (new ArrayList<ArrayList<CengSong>>(songs.subList(0, PlaylistNode.order))));
						newChild2 = new PlaylistNodeSecondaryLeaf(null, (new ArrayList<ArrayList<CengSong>>(songs.subList(PlaylistNode.order, 2 * PlaylistNode.order + 1))));
						/*children.add(new_leaf1);
						children.add(new_leaf2);

						secondaryRoot = new PlaylistNodeSecondaryIndex(null, genres, children);
						new_leaf1.setParent(secondaryRoot);
						new_leaf2.setParent(secondaryRoot);*/
						this.addKeyAnd2Children(nodeGenre, newChild1, newChild2);
						//songs.get(j).add(song);

				}
			}
		}
	}


	private void addKeyAnd2Children(String nodeNewYear,  PlaylistNode newChild1, PlaylistNode newChild2) {
		// if it is not full
		if (this.genreCount() < 2 * PlaylistNode.order) {
			Integer length, i;
			ArrayList<PlaylistNode> newChildren = this.children;
			length = this.genres.size();
			for (i = 0; i < length && this.genres.get(i).compareTo(nodeNewYear) < 0; i++) ;

			this.genres.add(i, nodeNewYear);
			newChild1.setParent(this);
			newChild2.setParent(this);
			newChildren.set(i, newChild1);
			newChildren.add(i + 1, newChild2);
			this.children = newChildren;
		}
		// if it is full
		else {
			ArrayList<String> newYears = this.genres;
			ArrayList<PlaylistNode> newChildren = this.children;
			Integer length, i, parentNewKey;
			String parentNewYear;
			PlaylistNodeSecondaryIndex parentNewChild;
			length = genres.size();
			for (i = 0; i < length && this.genres.get(i).compareTo(nodeNewYear) < 0; i++) ;

			newYears.add(i, nodeNewYear);
			newChildren.set(i, newChild1);
			newChildren.add(i + 1, newChild2);
		//	this.audioIDs = new ArrayList<Integer>(newKeys.subList(0, PlaylistNode.order));
			this.genres = new ArrayList<String>(newYears.subList(0, PlaylistNode.order));
			//parentNewKey = newKeys.get(PlaylistNode.order);
			parentNewYear = newYears.get(PlaylistNode.order);
			this.children = new ArrayList<PlaylistNode>(newChildren.subList(0, PlaylistNode.order + 1));
			parentNewChild = new PlaylistNodeSecondaryIndex(null, (new ArrayList<String>(newYears.subList(PlaylistNode.order + 1, 2 * PlaylistNode.order + 1))), (new ArrayList<PlaylistNode>(newChildren.subList(PlaylistNode.order + 1, 2 * PlaylistNode.order + 2))));
			if (i + 1 < PlaylistNode.order) {
				newChild1.setParent(this);
				newChild2.setParent(this);
			} else if (i < PlaylistNode.order) {
				newChild1.setParent(this);
				newChild2.setParent(parentNewChild);
			} else {
				newChild1.setParent(parentNewChild);
				newChild2.setParent(parentNewChild);
			}

			if (this.getParent() == null) {
				ArrayList<PlaylistNode> newParentChildren;
			//	ArrayList<Integer> newParentKeys;
				ArrayList<String> newParentYears;
				newParentChildren = new ArrayList<PlaylistNode>();
				newParentChildren.add((PlaylistNode) this);
				newParentChildren.add((PlaylistNode) parentNewChild);
			//	newParentKeys = new ArrayList<Integer>();
				newParentYears = new ArrayList<String>();
			//	newParentKeys.add(parentNewKey);
				newParentYears.add(parentNewYear);
				this.setParent((PlaylistNode) new PlaylistNodeSecondaryIndex(null, newParentYears, newParentChildren));
				parentNewChild.setParent(this.getParent());
			} else {
				((PlaylistNodeSecondaryIndex) this.getParent()).addKeyand1Child( parentNewYear, parentNewChild);
			}
		}
	}
		private void addKeyand1Child( String nodeNewYear,  PlaylistNodeSecondaryIndex newChild){
			// if it is not full
			if(this.genreCount() < 2*PlaylistNode.order){
				Integer length, i;
				ArrayList<PlaylistNode> newChildren = this.children;
				length = this.genres.size();
				for(i=0;  i<length && this.genres.get(i).compareTo(nodeNewYear)<0; i++);
				this.genres.add(i, nodeNewYear);
				newChild.setParent(this);
				newChildren.add(i+1, newChild);
				this.children = newChildren;
			}
			// if it is full
			else{
				Integer length, i, parentNewKey;
				String parentNewYear;
				ArrayList<PlaylistNode> newChildren = this.children;
				ArrayList<String> newYears = this.genres;
				PlaylistNodeSecondaryIndex parentNewChild;
				length = this.genres.size();
				for(i=0;  i<length && this.genres.get(i).compareTo(nodeNewYear)<0; i++);
				newYears.add(i, nodeNewYear);
				newChildren.add(i+1, newChild);
				this.genres = new ArrayList<String> (newYears.subList(0,PlaylistNode.order));
				parentNewYear = newYears.get(PlaylistNode.order);
				this.children = new ArrayList<PlaylistNode> (newChildren.subList(0,PlaylistNode.order+1));
				parentNewChild = new PlaylistNodeSecondaryIndex(null, (new ArrayList<String> (newYears.subList(PlaylistNode.order+1,2*PlaylistNode.order+1))), (new ArrayList<PlaylistNode> (newChildren.subList(PlaylistNode.order+1,2*PlaylistNode.order+2))));
				if (i+1 < PlaylistNode.order){
					newChild.setParent(this);
				}
				else {
					newChild.setParent(parentNewChild);
				}
				if(this.getParent() == null){
					ArrayList<PlaylistNode> newParentChildren;
					ArrayList<String> newParentYears;
					newParentChildren = new ArrayList<PlaylistNode>();
					newParentChildren.add(this);
					newParentChildren.add(parentNewChild);
					newParentYears = new ArrayList<String>();
					newParentYears.add(parentNewYear);
					this.setParent(new PlaylistNodeSecondaryIndex(null, newParentYears , newParentChildren));
					parentNewChild.setParent(this.getParent());
				}
				else{
					((PlaylistNodeSecondaryIndex)this.getParent()).addKeyand1Child( parentNewYear, parentNewChild);
				}
			}
		}

	public void print(){
		Integer length = this.genres.size(), i;
		System.out.println("<index>");
		for(i=0;  i<length; i++){
			System.out.println( this.genres.get(i) + "|" + this.genres.get(i));
		}
		System.out.println("</index>");
		// if its children are nodes
		if (this.children.get(0).getType() == PlaylistNodeType.Internal){
			for(PlaylistNode child:this.children){
				PlaylistNodeSecondaryIndex currentNode;
				currentNode = (PlaylistNodeSecondaryIndex) child;
				currentNode.print();
			}
		}
		// if its children are leaves
		else{
			for(PlaylistNode child:this.children){
				ArrayList<ArrayList<CengSong>> books;
				PlaylistNodeSecondaryLeaf currentLeaf;
				currentLeaf = (PlaylistNodeSecondaryLeaf) child;
				books = currentLeaf.getSongBucket();
				length = books.size();
				System.out.println("<data>");
				for(i=0; i<length; i++){
					for(int j=0; j<books.size(); j++){
					System.out.print("<record>");
					System.out.print(books.get(i).get(j).audioId());
					System.out.print("|");
					System.out.print(books.get(i).get(j).genre());
					System.out.print("|");
					System.out.print(books.get(i).get(j).songName());
					System.out.print("|");
					System.out.print(books.get(i).get(j).artist());
					System.out.println("</record>");
				}}
				System.out.println("</data>");
			}
		}
	}
	public ArrayList<Integer> getAllaudioIDs()
	{
		return this.audioIDs;
	}

	public ArrayList<String> getAllgenres()
	{
		return this.genres;
	}

	public void addaudio_id(Integer index, Integer key)
	{
		audioIDs.add(index,key);
	}

	public void addgenre(Integer key)
	{
		audioIDs.add(key);
	}

	public void addChild(Integer index, PlaylistNode child)
	{
		children.add(index,child);
	}

	public void addChild(PlaylistNode child)
	{
		children.add(child);
	}

	public void addgenre(Integer index, String year)
	{
		genres.add(index,year);
	}

	public void addgenre(String year)
	{
		genres.add(year);
	}

	public void setaudio_id(Integer index, Integer key)
	{
		audioIDs.set(index,key);
	}

	public void setChild(Integer index, PlaylistNode child)
	{
		children.set(index,child);
	}

	public void setgenre(Integer index, String year)
	{
		genres.set(index,year);
	}


}
