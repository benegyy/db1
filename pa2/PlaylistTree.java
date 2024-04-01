package student_pack;

import java.util.ArrayList;
import java.util.Stack;

public class PlaylistTree {

	public PlaylistNode primaryRoot;        //root of the primary B+ tree
	public PlaylistNode secondaryRoot;    //root of the secondary B+ tree

	public PlaylistTree(Integer order) {
		PlaylistNode.order = order;
		primaryRoot = new PlaylistNodePrimaryLeaf(null);
		primaryRoot.level = 0;
		secondaryRoot = new PlaylistNodeSecondaryLeaf(null);
		secondaryRoot.level = 0;
	}

	public void addSong(CengSong song) {
		// TODO: Implement this method

		if (primaryRoot.getType() == PlaylistNodeType.Leaf) {
			ArrayList<CengSong> songs;
			Integer length, i;

			if (((PlaylistNodePrimaryLeaf) primaryRoot).songCount() < 2 * PlaylistNode.order) {
				songs = ((PlaylistNodePrimaryLeaf) primaryRoot).getSongs();
				length = songs.size();

				for (i = 0; i < length && songs.get(i).audioId() < song.audioId(); i++) ;
				((PlaylistNodePrimaryLeaf) primaryRoot).addSong(i, song);

			} else {
				ArrayList<Integer> audio_ids;
				ArrayList<PlaylistNode> children;
				PlaylistNodePrimaryLeaf new_leaf1, new_leaf2;
				Integer nodeKey;

				songs = ((PlaylistNodePrimaryLeaf) primaryRoot).getSongs();
				length = songs.size();
				for (i = 0; i < length && songs.get(i).audioId() < song.audioId(); i++) ;
				songs.add(i, song);
				nodeKey = songs.get(PlaylistNode.order).audioId();
				audio_ids = new ArrayList<Integer>();
				audio_ids.add(nodeKey);
				children = new ArrayList<PlaylistNode>();
				new_leaf1 = new PlaylistNodePrimaryLeaf(null, (new ArrayList<CengSong>(songs.subList(0, PlaylistNode.order))));
				new_leaf2 = new PlaylistNodePrimaryLeaf(null, (new ArrayList<CengSong>(songs.subList(PlaylistNode.order, 2 * PlaylistNode.order + 1))));

				children.add(new_leaf1);
				children.add(new_leaf2);

				primaryRoot = new PlaylistNodePrimaryIndex(null, audio_ids, children);
				new_leaf1.setParent(primaryRoot);
				new_leaf2.setParent(primaryRoot);
			}
		} else {
			((PlaylistNodePrimaryIndex) primaryRoot).addSong(song);//****
			if (primaryRoot.getParent() != null) {
				primaryRoot = primaryRoot.getParent();
			}
		}

		if (secondaryRoot.getType() == PlaylistNodeType.Leaf) {
			ArrayList<ArrayList<CengSong>> genres;
			Integer length, i;
			boolean a = true;
			Integer j;
			genres = ((PlaylistNodeSecondaryLeaf) secondaryRoot).getSongBucket();
			length = genres.size();

			for (j = 0; j < length ; j++) {
				if(genres.get(j).get(0).genre().compareTo(song.genre()) == 0){
				if (genres.get(j).get(0).genre().compareTo(song.genre()) == 0) {
					genres.get(j).add(song);
					a = false;
					return;}
				}
			}


			if (((PlaylistNodeSecondaryLeaf) secondaryRoot).genreCount() < 2 * PlaylistNode.order) {

				genres = ((PlaylistNodeSecondaryLeaf) secondaryRoot).getSongBucket();
				length = genres.size();

				for (i = 0; i < length && genres.get(i).get(0).genre().compareTo(song.genre()) < 0; i++) ;
				((PlaylistNodeSecondaryLeaf) secondaryRoot).addSong(i, song);
			}
			else {


				ArrayList<String> genress;
				ArrayList<PlaylistNode> children;
				PlaylistNodeSecondaryLeaf new_leaf1, new_leaf2;
				String nodeGenre;
				//boolean a = true;
				//Integer j;

				genres = ((PlaylistNodeSecondaryLeaf) secondaryRoot).getSongBucket();

				length = genres.size();
				for (j = 0; j < length && genres.get(j).get(0).genre().compareTo(song.genre()) < 0; j++) {
					/*if (genres.get(j+1).get(0).genre().compareTo(song.genre()) == 0) {
						genres.get(j).add(song);
						a = false;
						break;
					}*/
				}
				if (a) {


					ArrayList<CengSong> new_genre;
					new_genre = new ArrayList<>();
					new_genre.add(song);
					nodeGenre = genres.get(j+1).get(0).genre();
					genres.add(j, new_genre);


					//nodeGenre = genres.get(j).get(0).genre();
					genress = new ArrayList<String>();
					genress.add(nodeGenre);
					children = new ArrayList<PlaylistNode>();
					new_leaf1 = new PlaylistNodeSecondaryLeaf(null, (new ArrayList<ArrayList<CengSong>>(genres.subList(0, PlaylistNode.order))));
					new_leaf2 = new PlaylistNodeSecondaryLeaf(null, (new ArrayList<ArrayList<CengSong>>(genres.subList(PlaylistNode.order, 2 * PlaylistNode.order + 1))));

					children.add(new_leaf1);
					children.add(new_leaf2);

					secondaryRoot = new PlaylistNodeSecondaryIndex(null, genress, children);
					new_leaf1.setParent(secondaryRoot);
					new_leaf2.setParent(secondaryRoot);
				}
			}

		} else {


			((PlaylistNodeSecondaryIndex) secondaryRoot).addSong(song);
			if (secondaryRoot.getParent() != null) {
				secondaryRoot = secondaryRoot.getParent();
			}
		}
	}

	public CengSong searchSong(Integer audioId) {
		// TODO: Implement this method
		PlaylistNode node = primaryRoot;
		boolean cont;

		while (node.getType() == PlaylistNodeType.Internal)
		{
			System.out.println("<index>");
			for (int i = 0; i < ((PlaylistNodePrimaryIndex) node).audioIdCount(); i++) {
				System.out.println(((PlaylistNodePrimaryIndex) node).audioIdAtIndex(i));
			}
			System.out.println("</index>");
			cont = false;
			for (int i = 0; i < ((PlaylistNodePrimaryIndex) node).audioIdCount() ; i++) {
				// if ith key is greater than our key, then i is the index of next child
				if (((PlaylistNodePrimaryIndex) node).audioIdAtIndex(i) > audioId)
				{
					node = ((PlaylistNodePrimaryIndex) node).getChildrenAt(i);
					cont = true;
					break;
				}
			}
			if (!cont)
				node = ((PlaylistNodePrimaryIndex) node).getChildrenAt(((PlaylistNodePrimaryIndex) node).audioIdCount());
		}
		for (int i = 0; i < ((PlaylistNodePrimaryLeaf) node).songCount(); i++) {
			if (((PlaylistNodePrimaryLeaf) node).audioIdAtIndex(i) == audioId)
			{
				System.out.println("<data>");
				System.out.print("<record>");
				System.out.print(((PlaylistNodePrimaryLeaf) node).songAtIndex(i).audioId());
				System.out.print("|");
				System.out.print(((PlaylistNodePrimaryLeaf) node).songAtIndex(i).genre());
				System.out.print("|");
				System.out.print(((PlaylistNodePrimaryLeaf) node).songAtIndex(i).songName());
				System.out.print("|");
				System.out.print(((PlaylistNodePrimaryLeaf) node).songAtIndex(i).artist());
				System.out.print("</record>\n");
				System.out.println("</data>");
				return (((PlaylistNodePrimaryLeaf) node).songAtIndex(i));
			}
		}
		System.out.print("Could not find ");
		System.out.println(audioId);
		return null;

	}
	
	
	public void printPrimaryPlaylist() {
		// TODO: Implement this method
		Stack<PlaylistNode> itemStack = new Stack<PlaylistNode>();
		PlaylistNode pTreeRoot = primaryRoot;

		itemStack.add(pTreeRoot);
		int k=0,j;

		while (!itemStack.isEmpty())
		{
			PlaylistNode node = itemStack.pop();
			if (node.getType() == PlaylistNodeType.Internal)
			{

				ArrayList<PlaylistNode> children = ((PlaylistNodePrimaryIndex) node).getAllChildren();
				for (int i = children.size() - 1; i >= 0 ; i--)
					itemStack.add(children.get(i));
				j=k;
					if(j!=0) j=k/6+1;
					System.out.println("\t".repeat(j) + "<index>");
					for (int i = 0; i < ((PlaylistNodePrimaryIndex) node).audioIdCount(); i++)
						System.out.println("\t".repeat(j) + ((PlaylistNodePrimaryIndex) node).audioIdAtIndex(i));
					System.out.println("\t".repeat(j) +"</index>");


				k++;

			}

			else if (node.getType() == PlaylistNodeType.Leaf)
			{
				j=k;
				if(j!=0) j=k/6+2;
				System.out.println("\t".repeat(j)+"<data>");
				for (int i = 0; i < ((PlaylistNodePrimaryLeaf) node).songCount() ; i++)
				{
					System.out.print("\t".repeat(j)+"<record>");
					System.out.print(((PlaylistNodePrimaryLeaf) node).songAtIndex(i).audioId());
					System.out.print("|");
					System.out.print(((PlaylistNodePrimaryLeaf) node).songAtIndex(i).genre());
					System.out.print("|");
					System.out.print(((PlaylistNodePrimaryLeaf) node).songAtIndex(i).songName());
					System.out.print("|");
					System.out.print(((PlaylistNodePrimaryLeaf) node).songAtIndex(i).artist());
					System.out.print("</record>\n");
				}
				System.out.println("\t".repeat(j)+"</data>");
			}

		}
	}
	
	public void printSecondaryPlaylist() {
		// TODO: Implement this method
		Stack<PlaylistNode> itemStack = new Stack<PlaylistNode>();
		PlaylistNode pTreeRoot = secondaryRoot;

		itemStack.add(pTreeRoot);
		Integer k=0,u;
		while (!itemStack.isEmpty())
		{
			PlaylistNode node = itemStack.pop();
			if (node.getType() == PlaylistNodeType.Internal)
			{
				ArrayList<PlaylistNode> children = ((PlaylistNodeSecondaryIndex) node).getAllChildren();
				for (int i = children.size() - 1; i >= 0 ; i--)
					itemStack.add(children.get(i));
				u=k;
				if(u!=0) u=k/6+1;

				System.out.println("\t".repeat(u)+"<index>");
				for (int i = 0; i < ((PlaylistNodeSecondaryIndex) node).genreCount() ; i++)
				{
					System.out.println(((PlaylistNodeSecondaryIndex) node).genreAtIndex(i));
				}
				System.out.println("\t".repeat(u)+"</index>");
				k++;
			}

			else if (node.getType() == PlaylistNodeType.Leaf)
			{
				// print all books, from left to right
				u=k;
				if(u!=0) u=k/6+1;
				System.out.println("\t".repeat(u)+"<data>");
				for (int i = 0; i < ((PlaylistNodeSecondaryLeaf) node).genreCount() ; i++)
				{

				System.out.println("\t".repeat(u)+((PlaylistNodeSecondaryLeaf)node).getSongBucket().get(i).get(0).genre());
					for(int j=0 ; j<((PlaylistNodeSecondaryLeaf) node).getSongBucket().get(i).size();j++){
						System.out.print("\t".repeat(u+1)+"<record>");
						System.out.print(((PlaylistNodeSecondaryLeaf) node).getSongBucket().get(i).get(j).audioId());
						System.out.print("|");
						System.out.print(((PlaylistNodeSecondaryLeaf) node).getSongBucket().get(i).get(j).genre());
						System.out.print("|");
						System.out.print(((PlaylistNodeSecondaryLeaf) node).getSongBucket().get(i).get(j).songName());
						System.out.print("|");
						System.out.print(((PlaylistNodeSecondaryLeaf) node).getSongBucket().get(i).get(j).artist());
						System.out.print("</record>\n");
					}

				}
				System.out.println("\t".repeat(u)+"</data>");
			}
		}
	}

	
	// Extra functions if needed


}


