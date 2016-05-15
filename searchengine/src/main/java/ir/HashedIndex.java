/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  


package ir;

import java.util.HashMap;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.Comparator;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.nio.file.Files;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    private boolean scanFiles = false;

    /** How many files in case of saving the index on hard drive */
    public static final int INDEX_SAVE_N_FILES = 1000;

    /** How many elements in champions lists */
    public static final int CHAMP_LIST_SIZE = 200;

    public static HashMap<Integer, Double> pageRanks = new HashMap<Integer, Double>();

    public int countDoc = 0;

    /**
     *  Inserts this token in the index.
     */
    public void insert( String token, int docID, int offset ) {

        if(docID > 100000)
            return;
        
        PostingsList pl;

        if(( pl = index.get(token)) == null) {
            pl = new PostingsList();
        }

        pl.add(docID, offset);

        Index.index.put(token, pl);
    }


    /**
     *  Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {
	    return index.keySet().iterator();
    }


    /**
     *  Returns the postings for a specific term, or null
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        return index.get(token);
    }

    /**
     * Compute tfidf for all PostingsEntry in the index
     */
    public static void computeAllTfidf() {
        HashMap<Integer, Double> docNorms = new HashMap<Integer, Double>();
        Double d;

        for(PostingsList pl: Index.index.values()) {
            for(PostingsEntry pe: pl.list) {
                pe.score_tfidf = pe.tfidf(pl.list.size());

                if((d = docNorms.get(pe.docID)) == null) {
                    docNorms.put(pe.docID, pe.score_tfidf * pe.score_tfidf);
                } else {
                    docNorms.put(pe.docID, d + pe.score_tfidf * pe.score_tfidf);
                }
            }
        }

        for(Map.Entry<Integer, Double> e: docNorms.entrySet()) {
            docNorms.put(e.getKey(), Math.sqrt(e.getValue()));
        }

        RankedQuery.docNorms = docNorms;
    }

    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {
        long t1 = System.nanoTime();
        ArrayList<PostingsList> responses = new ArrayList<PostingsList>(query.size());
        PostingsList resp = null;

        responses = new ArrayList<PostingsList>(query.size());
        PostingsList l;
        for(int i = 0 ; i < query.size() ; i++) {
            if((l = index.get(query.terms.get(i))) == null) {
                l = new PostingsList();
            }
            responses.add(l);
        }

        switch(queryType) {
            case Index.INTERSECTION_QUERY:
            case Index.PHRASE_QUERY:
                resp =  IntersectionQuery.intersectResponses(responses, queryType);
                break;
            case Index.RANKED_QUERY:
                resp =  RankedQuery.rankByScore(query, responses, rankingType);
        }

        System.out.println("Qsize: " + query.size() + " / Processing time: " + (System.nanoTime() - t1) + " ns");

        return resp;
    }

    public static void computeChampionsLists() {
        // Compute champions lists for all postingsList in the index
        for(PostingsList pl: Index.index.values()) {
            pl.computeChampionsList();
        }
    }

    public void cleanup() {

        int nFiles = 100;

        PrintWriter writer;

        try {
            FileWriter f;
            PostingsList pl;
            PostingsEntry pe;
            int s;
            int sOffsets;

            // Saving document names
            f = new FileWriter("savedindex/__docnames__");
            writer = new PrintWriter(new BufferedWriter(f));
            for(Map.Entry<String, String> e : Index.docIDs.entrySet()) {
                writer.print(e.getKey());
                writer.print("|");
                writer.println(e.getValue());
            }
            writer.close();

            // Saving document lengths
            f = new FileWriter("savedindex/__doclengths__");
            writer = new PrintWriter(new BufferedWriter(f));
            for(Map.Entry<String, Integer> e : Index.docLengths.entrySet()) {
                writer.print(e.getKey());
                writer.print("|");
                writer.println(e.getValue());
            }
            writer.close();

            // Saving docTimeFrames
            f = new FileWriter("savedindex/__doctimeframes__");
            writer = new PrintWriter(new BufferedWriter(f));
            for(Map.Entry<String, Integer> e : Index.docLengths.entrySet()) {
                writer.print(e.getKey());
                writer.print("|");
                writer.println(e.getValue());
            }
            writer.close();

            // Saving postings lists & scores
            for(Map.Entry<String, PostingsList> e : Index.index.entrySet()) {
                String key = e.getKey();
                PostingsList val = e.getValue();

                int h = key.hashCode();
                String fileName = ((Integer)Math.abs(h % INDEX_SAVE_N_FILES)).toString();
                f = new FileWriter("savedindex/" + fileName, true);
                writer = new PrintWriter(new BufferedWriter(f));

                s = val.size();
                writer.print(key + "|");
                for(int k = 0 ; k < s ; k++) {
                    pe = val.get(k);
                    writer.print(pe.docID + ":");

                    sOffsets = pe.size();
                    for(int j = 0 ; j < sOffsets ; j++) {
                        writer.print(pe.get(j));

                        if(j < sOffsets - 1) {
                            writer.print(",");
                        }
                    }

                    if(k < s - 1) {
                        writer.print(";");
                    }
                }
                writer.println();
                writer.close();
            }


        } catch(Exception e) {
            System.err.println(e.getMessage());
        }

    }

    public void scanFilesTrue() {
        this.scanFiles = true;
    }
}
