package student_pack;

import java.util.ArrayList;

public class PlaylistNodePrimaryIndex extends PlaylistNode {
	private ArrayList<Integer> audioIds;
	private ArrayList<PlaylistNode> children;
	
	public PlaylistNodePrimaryIndex(PlaylistNode parent) {
		super(parent);
		audioIds = new ArrayList<Integer>();
		children = new ArrayList<PlaylistNode>();
		this.type = PlaylistNodeType.Internal;
	}
	
	public PlaylistNodePrimaryIndex(PlaylistNode parent, ArrayList<Integer> audioIds, ArrayList<PlaylistNode> children) {
		super(parent);
		this.audioIds = audioIds;
		this.children = children;
		this.type = PlaylistNodeType.Internal;
	}
	
	// GUI Methods - Do not modify
	public ArrayList<PlaylistNode> getAllChildren()
	{
		return this.children;
	}
	
	public PlaylistNode getChildrenAt(Integer index) {return this.children.get(index); }
	
	public Integer audioIdCount()
	{
		return this.audioIds.size();
	}
	public Integer audioIdAtIndex(Integer index) {
		if(index >= this.audioIdCount() || index < 0) {
			return -1;
		}
		else {
			return this.audioIds.get(index);
		}
	}

	// Extra functions if needed
	public void addSong( CengSong song){
		if(this.children.get(0).type == PlaylistNodeType.Internal){
			int length, i;
			length = this.audioIds.size();
			for(i=0 ; i<length && this.audioIds.get(i)<song.audioId(); i++);
				((PlaylistNodePrimaryIndex)this.children.get(i)).addSong(song);
			}
			else{
				ArrayList<CengSong> songs;
				int i, length;
				length = this.audioIds.size();

				for(i=0; i<length && this.audioIds.get(i)<song.audioId(); i++);

				if(((PlaylistNodePrimaryLeaf)this.children.get(i)).songCount() < 2*PlaylistNode.order){
					int j;
					songs = (((PlaylistNodePrimaryLeaf) this.children.get(i)).getSongs());
					length = songs.size();

					for(j=0; j<length && songs.get(j).audioId() < song.audioId(); j++);
					((PlaylistNodePrimaryLeaf)this.children.get(i)).addSong(j,song);


			}

				else{
					PlaylistNodePrimaryLeaf newchild1, newchild2, currentLeaf;
					int newNodeKey, j;
					currentLeaf =(PlaylistNodePrimaryLeaf)this.children.get(i);

					songs = currentLeaf.getSongs();
					length=songs.size();

					for(j=0;j<length && songs.get(j).audioId()<song.audioId();j++);
					songs.add(j,song);

					newchild1 = new PlaylistNodePrimaryLeaf(null,(new ArrayList<CengSong>(songs.subList(0,PlaylistNode.order))));
					newchild2 = new PlaylistNodePrimaryLeaf(null, (new ArrayList<CengSong>(songs.subList(PlaylistNode.order,2*PlaylistNode.order+1))));
					newNodeKey = songs.get(PlaylistNode.order).audioId();
					this.addKeyAnd2Children(newNodeKey,newchild1,newchild2);
				}
		}
	}
 public void addKeyAnd2Children(int nodeNewKey, PlaylistNode newChild1, PlaylistNode newChild2){

		if(this.audioIdCount() <2*PlaylistNode.order){
			int length, i;
			ArrayList<PlaylistNode> newChildren = this.children;
			length = this.audioIds.size();
			for(i=0;i<length && this.audioIds.get(i)<nodeNewKey;i++);
			this.audioIds.add(i, nodeNewKey);
			newChild1.setParent(this);
			newChild2.setParent(this);
			newChildren.set(i,newChild1);
			newChildren.add(i+1,newChild2);
			this.children = newChildren;

		}
		else{
			ArrayList<Integer> newKeys = this.audioIds;
			ArrayList<PlaylistNode> newChildren = this.children;
			int length, i, parentNewKey;
			PlaylistNodePrimaryIndex parentNewChild;
			length = audioIds.size();
			for(i=0;i<length && newKeys.get(i)< nodeNewKey; i++);
			newKeys.add(i,nodeNewKey);
			newChildren.set(i, newChild1);
			newChildren.add(i+1,newChild2);

			this.audioIds = new ArrayList<Integer>(newKeys.subList(0,PlaylistNode.order));
			parentNewKey = newKeys.get(PlaylistNode.order);
			this.children = new ArrayList<PlaylistNode>(newChildren.subList(0,PlaylistNode.order+1));
			parentNewChild = new PlaylistNodePrimaryIndex(null, (new ArrayList<Integer>(newKeys.subList(PlaylistNode.order+1, 2*PlaylistNode.order+1))), (new ArrayList<PlaylistNode>(newChildren.subList(PlaylistNode.order+1, 2*PlaylistNode.order+2))));

			if(i+1 < PlaylistNode.order){
				newChild1.setParent(this);
				newChild2.setParent(this);
			}
			else if( i< PlaylistNode.order){
				newChild1.setParent(this);
				newChild2.setParent(parentNewChild);
			}
			if(this.getParent() == null){
				ArrayList<PlaylistNode> newParentChildren;
				ArrayList<Integer> newParentKeys;
				newParentChildren = new ArrayList<PlaylistNode>();
				newParentChildren.add((PlaylistNode) this);
				newParentChildren.add((PlaylistNode) parentNewChild);
				newParentKeys = new ArrayList<Integer>();
				newParentKeys.add(parentNewKey);
				this.setParent((PlaylistNode) new PlaylistNodePrimaryIndex(null, newParentKeys , newParentChildren));
				parentNewChild.setParent(this.getParent());
			}
			else{
				((PlaylistNodePrimaryIndex)this.getParent()).addKeyand1Child(parentNewKey, parentNewChild);
			}
		}
 }
	private void addKeyand1Child(int nodeNewKey, PlaylistNodePrimaryIndex newChild){
		if(this.audioIdCount() < 2*PlaylistNode.order){
			int length, i;
			ArrayList<PlaylistNode> newChildren = this.children;
			length = this.audioIds.size();
			for(i=0;  i<length && this.audioIds.get(i)<nodeNewKey; i++);
			this.audioIds.add(i, nodeNewKey);
			newChild.setParent(this);
			newChildren.add(i+1, newChild);
			this.children = newChildren;
		}
		else{
			int length, i, parentNewKey;
			ArrayList<PlaylistNode> newChildren = this.children;
			ArrayList<Integer> newKeys = this.audioIds;
			PlaylistNodePrimaryIndex parentNewChild;
			length = this.audioIds.size();
			for(i=0;  i<length && this.audioIds.get(i)<nodeNewKey; i++);
			newKeys.add(i, nodeNewKey);
			newChildren.add(i+1, newChild);
			this.audioIds = new ArrayList<Integer> (newKeys.subList(0,PlaylistNode.order));
			parentNewKey = newKeys.get(PlaylistNode.order);
			this.children = new ArrayList<PlaylistNode> (newChildren.subList(0,PlaylistNode.order+1));
			parentNewChild = new PlaylistNodePrimaryIndex(null, (new ArrayList<Integer> (newKeys.subList(PlaylistNode.order+1,2*PlaylistNode.order+1))), (new ArrayList<PlaylistNode> (newChildren.subList(PlaylistNode.order+1,2*PlaylistNode.order+2))));
			if (i+1 < PlaylistNode.order){
				newChild.setParent(this);
			}
			else {
				newChild.setParent(parentNewChild);
			}
			if(this.getParent() == null){
				ArrayList<PlaylistNode> newParentChildren;
				ArrayList<Integer> newParentKeys;
				newParentChildren = new ArrayList<PlaylistNode>();
				newParentChildren.add(this);
				newParentChildren.add(parentNewChild);
				newParentKeys = new ArrayList<Integer>();
				newParentKeys.add(parentNewKey);
				this.setParent(new PlaylistNodePrimaryIndex(null, newParentKeys , newParentChildren));
				parentNewChild.setParent(this.getParent());
			}
			else{
				((PlaylistNodePrimaryIndex)this.getParent()).addKeyand1Child(parentNewKey, parentNewChild);
			}
		}
	}
	public CengSong searchSong (int key) {
		PlaylistNode result = null;
		int length = this.audioIds.size(), i;
		System.out.println("<index>");
		for (i = 0; i < length; i++) {
			System.out.println(this.audioIds.get(i));
			if (this.audioIds.get(i) <= key) {
				result = children.get(i + 1);
			}
		}
		System.out.println("</index>");
		// if its children are nodes
		if (this.children.get(0).type == PlaylistNodeType.Internal) {
			PlaylistNodePrimaryIndex currentNode;
			currentNode = (PlaylistNodePrimaryIndex) result;
			if (result != null) {
				return currentNode.searchSong(key);
			} else {
				return ((PlaylistNodePrimaryIndex) this.children.get(0)).searchSong(key);
			}
		} else {
			ArrayList<CengSong> books;
			PlaylistNodePrimaryLeaf currentLeaf;
			if (result == null) {
				result = children.get(0);
			}
			currentLeaf = (PlaylistNodePrimaryLeaf) result;
			books = currentLeaf.getSongs();
			length = books.size();
			for (i = 0; i < length; i++) {
				if (books.get(i).audioId() == key) {
					System.out.println("<data>");
					System.out.print("<record>");
					System.out.print(books.get(i).audioId());
					System.out.print("|");
					System.out.print(books.get(i).songName());
					System.out.print("|");
					System.out.print(books.get(i).artist());
					System.out.print("|");
					System.out.print(books.get(i).genre());
					System.out.println("</record>");
					System.out.println("</data>");
					return books.get(i);
				}
			}
			System.out.print("No match for ");
			System.out.println(key);
			return null;
		}
	}
		public void print(){
			int length = this.audioIds.size(), i;
			System.out.println("<index>");
			for(i=0;  i<length; i++){
				System.out.println(this.audioIds.get(i));
			}
			System.out.println("</index>");
			// if its children are nodes
			if (this.children.get(0).getType() == PlaylistNodeType.Internal){
				for(PlaylistNode child:this.children){
					PlaylistNodePrimaryIndex currentNode;
					currentNode = (PlaylistNodePrimaryIndex) child;
					currentNode.print();
				}
			}
			// if its children are leaves
			else{
				for(PlaylistNode child:this.children){
					ArrayList<CengSong> books;
					PlaylistNodePrimaryLeaf currentLeaf;
					currentLeaf = (PlaylistNodePrimaryLeaf) child;
					books = currentLeaf.getSongs();
					length = books.size();
					System.out.println("<data>");
					for(i=0; i<length; i++){
						System.out.print("<record>");
						System.out.print(books.get(i).audioId());
						System.out.print("|");
						System.out.print(books.get(i).songName());
						System.out.print("|");
						System.out.print(books.get(i).artist());
						System.out.print("|");
						System.out.print(books.get(i).genre());
						System.out.println("</record>");
					}
					System.out.println("</data>");
				}
			}
		}
	public ArrayList<Integer> getAllKeys()
	{
		return this.audioIds;
	}

	public void addKey(Integer index, Integer key)
	{
		audioIds.add(index,key);
	}

	public void addKey(Integer key)
	{
		audioIds.add(key);
	}

	public void addChild(Integer index, PlaylistNode child)
	{
		children.add(index,child);
	}

	public void addChild(PlaylistNode child)
	{
		children.add(child);
	}

	public void setKey(Integer index, Integer key)
	{
		audioIds.set(index,key);
	}

	public void setChild(Integer index, PlaylistNode child)
	{
		children.set(index,child);
	}


	}

