package ir;

import java.util.Iterator;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.FileReader;

class Similarity {

    public static double tfidf(Index idx, String word, int doc) {

        double tf = 0;
        PostingsList pl = idx.getPostings(word);

        for(PostingsEntry pe : pl.list) {
            if(pe.docID == doc) {
                tf = (double)pe.size();
                break;
            }
        }

        double idf = Math.log( (double)Index.docIDs.size() / (double)pl.size() );

        return tf * idf;
    }

    public static double cosine(int docID, Query query) {

        Iterator<String> tokens = Similarity.tokenizeDoc(docID);
        

        while(tokens.hasNext()) {

        }

        return 0.0;
    }

    public static Iterator<String> tokenizeDoc(int docID) {
        String fileName = Index.docIDs.get(((Integer)docID).toString());
        String line;
        BufferedReader reader;
        LinkedList<String> tokens = new LinkedList<String>();

        try {

            reader = new BufferedReader(new FileReader("davisWiki/" + fileName));
            while((line = reader.readLine()) != null) {
                String[] words = line.split(" ");
                for(String w : words) {
                    tokens.addLast(w);
                }
            }
        } catch(Exception e) {
            System.err.println(e.getMessage());
        }

        return tokens.iterator();
    }

}
