package ir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class RankedQuery {

    // balancing PR and TFIDF. Choose 1.0 for PR only / 0.0 for tfidf only
    private static final double prVStfidf = 0.95;

    public static final boolean CHAMPIONS_LIST = true;

    public static HashMap<Integer, Double> docNorms;

    /**
     * Returns a PostingsList of relevant articles ranked by score
     */
    public static PostingsList rankByScore(Query query, ArrayList<PostingsList> lists, int rankingType) {

        PostingsList docScores = null;

        // Get score for each doc
        switch(rankingType) {
            case Index.TF_IDF:
                docScores = RankedQuery.tfidf(query, lists);
                break;
            case Index.PAGERANK:
                docScores = RankedQuery.pageRank(lists);
                break;
            case Index.COMBINATION:
                docScores = RankedQuery.combination(query, lists, prVStfidf);
        }

        // Sort PostingsEntries by score now:
        Collections.sort(docScores.list);

        return docScores;
    }

    private static PostingsList tfidf(Query query, ArrayList<PostingsList> lists) {
        HashMap<Integer, PostingsEntry> sum = new HashMap<Integer, PostingsEntry>();
        PostingsList docScores = new PostingsList();
        Double tfidf;
        PostingsEntry p;
        List<PostingsEntry> l = null;

        query.print();

        // Computing cosine similarity from the query terms weights
        for(int i = 0 ; i < lists.size() ; i++) {

            // Champions list or full list:
            if(RankedQuery.CHAMPIONS_LIST) {
                l = lists.get(i).championsList;
            } else {
                l = lists.get(i).list;
            }

            for(PostingsEntry pe : l) {
                tfidf = pe.score_tfidf;

                if((p = sum.get(pe.docID)) == null) {
                    p = new PostingsEntry();
                    p.docID = pe.docID;
                    p.score = tfidf * query.weights.get(i);
                    sum.put(pe.docID, p);
                } else {
                    p.score += tfidf * query.weights.get(i);
                }
            }
        }

        for(HashMap.Entry<Integer, PostingsEntry> e : sum.entrySet()) {
            e.getValue().score = e.getValue().score / (Math.sqrt(lists.size()) * RankedQuery.docNorms.get(e.getValue().docID));
            docScores.add(e.getValue());
        }

        return docScores;
    }

    private static PostingsList pageRank(ArrayList<PostingsList> lists) {
        PostingsList pr = new PostingsList();
        for(PostingsList pl: lists) {
            for(PostingsEntry pe: pl.list) {
                try {
                    pe.score = HashedIndex.pageRanks.get(pe.docID);
                } catch (Exception e) {
                    System.err.println("No page rank found for article " + pe.docID);
                    pe.score = 0.0;
                }
                pr.add(pe);
            }
        }

        return pr;
    }

    private static PostingsList combination(Query query, ArrayList<PostingsList> lists, double a) {
        PostingsList tfidf = RankedQuery.tfidf(query, lists);

        // Checking values for a
        if(a < 0) {
            a = 0;
        } else if(a > 1) {
            a = 1;
        }

        // Updating scores taking PR into account
        ListIterator<PostingsEntry> it = tfidf.list.listIterator();
        while(it.hasNext()) {
            PostingsEntry pe = it.next();
            pe.score = (1 - a) * pe.score + a * HashedIndex.pageRanks.get(pe.docID);
        }

        return tfidf;
    }

}
