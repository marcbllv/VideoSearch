/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;


/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {
    
    /** The postings list as a linked list. */
    public LinkedList<PostingsEntry> list;
    public List<PostingsEntry> championsList;

    PostingsList() {
        this.list = new LinkedList<PostingsEntry>();
    }

    /**  Number of postings in this list  */
    public int size() {
        return this.list.size();
    }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) {
        return list.get( i );
    }

    /** 
     * Add a new entry
     **/ 
    public void add(int docID, int offset) {
        // Hack: docs coming with increasing docID so this makes things faster
        // by checking first if doc can be added directly to the end
        if(this.list.size() > 0) {
            if(this.list.getLast().docID < docID) {
                PostingsEntry newPE = new PostingsEntry(docID);
                newPE.offsets.add(offset);
                this.list.addLast(newPE);
                return;
            }

            if(this.list.getLast().docID == docID) {
                this.list.getLast().offsets.add(offset);
                return;
            }
        }

        ListIterator<PostingsEntry> it = this.list.listIterator();

        while(it.hasNext()) {
            PostingsEntry p = it.next();

            if(p.docID >= docID) {
                if(p.docID == docID) {
                    p.offsets.add(offset);
                    it.previous();
                } else {
                    PostingsEntry newPE = new PostingsEntry(docID);
                    newPE.offsets.add(offset);

                    it.previous();
                    it.add(newPE);
                }

                break;
            }
        }

        if(!it.hasNext()) {
            PostingsEntry newPE = new PostingsEntry(docID);
            newPE.offsets.add(offset);
            this.list.addLast(newPE);
        }

    }

    public void computeChampionsList() {
        this.championsList = new LinkedList<PostingsEntry>(this.list);
        Collections.sort(this.championsList, new Comparator<PostingsEntry>() {
            public int compare(PostingsEntry p1, PostingsEntry p2) {
                return p2.offsets.size() - p1.offsets.size(); // Opposite as std comparator to sort in descending order
            }
        });

        int size = (HashedIndex.CHAMP_LIST_SIZE < this.championsList.size() ? HashedIndex.CHAMP_LIST_SIZE : this.championsList.size());

        this.championsList = this.championsList.subList(0, size);
    }

    public void add(PostingsEntry e) {
        this.list.add(e);
    }

    public void println() {
        for(PostingsEntry pe : list) {
            System.out.print(pe.docID + ":");
            for(int o : pe.offsets) {
                System.out.print(o + ",");
            }
            System.out.print(" | ");
        }
        System.out.println();
    }

    public double getTfidf(int docID) {

        for(PostingsEntry pe : this.list) {
            if(pe.docID == docID) {
                return pe.tfidf(this.list.size());
            }
        }

        // Word doesn't appear in doc: tfidf = 0
        return 0.0;
    }
}

