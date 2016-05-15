/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.ArrayList;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    public int docID;
    public double score;
    public double score_tfidf;
    public double[] tfidf_vect;
    public LinkedList<Integer> offsets;

    PostingsEntry() {
        this.offsets = new LinkedList<Integer>();
    }

    PostingsEntry(int docID) {
        this.docID = docID;
        this.offsets = new LinkedList<Integer>();
    }

    PostingsEntry(int docID, double score) {
        this.docID = docID;
        this.offsets = new LinkedList<Integer>();
    }

    public PostingsEntry clone() {
        PostingsEntry pe = new PostingsEntry();
        pe.docID = this.docID;
        pe.score = this.score;
        pe.score_tfidf = this.score_tfidf;
        pe.tfidf_vect = this.tfidf_vect; // Shallow copy for tfidf_vect
        pe.offsets = this.offsets; // Shallow copy for offsets, its okay

        return pe;
    }

    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) {
        return Double.compare( other.score, score );
    }

    @Override
    public boolean equals(Object e) {
        return ((PostingsEntry)e).docID == this.docID;
    }

    @Override
    public int hashCode() {
        return this.docID;
    }

    public int size() {
        return this.offsets.size();
    }

    public int get(int i) {
        return this.offsets.get(i);
    }

    public void println() {
        System.out.print(this.docID + ":");
        for(int o : this.offsets) {
            System.out.print(o + ",");
        }
        System.out.println();
    }

    public double tfidf(int docCount) {
        double docLength = Index.docLengths.get(((Integer)this.docID).toString()); 
        double tf = this.size() / docLength;
        double idf = Index.docIDs.size() / docCount;

        return tf * Math.log(idf);
    }
}

    
