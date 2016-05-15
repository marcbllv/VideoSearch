/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */  

package ir;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.Map;

public class Query {

    public static final double alpha = 1.0;
    public static final double beta  = 1.0;
    
    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();

    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
	
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
        StringTokenizer tok = new StringTokenizer( queryString );
        while ( tok.hasMoreTokens() ) {
            terms.add( tok.nextToken() );
            weights.add( new Double(1) );
        }    
    }

    /**
     * Print terms and weights
     */
    public void print() {
        ListIterator<String> itTerms = this.terms.listIterator();
        ListIterator<Double> itWeigh = this.weights.listIterator();

        System.out.println();
        System.out.println("Current query:");
        while(itTerms.hasNext()) {
            System.out.println(itWeigh.next() + "   " + itTerms.next());
        }
        System.out.println();
    }
    
    /**
     *  Returns the number of terms
     */
    public int size() {
	return terms.size();
    }
    
    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy() {
        Query queryCopy = new Query();
        queryCopy.terms = (LinkedList<String>) terms.clone();
        queryCopy.weights = (LinkedList<Double>) weights.clone();
        return queryCopy;
    }

    /**
     * Normalize weights vector to 1
     */
    public void normalize() {
        double norm = .0;
        for(Double d: this.weights) {
            norm += d * d;
        }
        norm = Math.sqrt(norm);
        for(ListIterator<Double> it = this.weights.listIterator() ; it.hasNext() ; ) {
            it.set(it.next() / norm);
        }
    }

    /**
     *  Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {
        ArrayList<Double> relevantSum = new ArrayList<Double>(this.size());
        Double s;
        int relevantCount = 0;

        // Normalizing query & multiplication by alpha:
        this.normalize();
        ListIterator<Double> qW = this.weights.listIterator();
        while(qW.hasNext()) {
            qW.set(Query.alpha * qW.next());
        }

        this.print();

        for(int i = 0 ; i < 10 ; i++) {
            relevantCount += docIsRelevant[i] ? 1 : 0;
        }

        for(int i = 0 ; i < 10 ; i++) {
            if(results.get(i) == null) {
                break; // less than 10 results
            }

            Query docVect = this.copy();

            if(docIsRelevant[i]) {
                System.out.println("Doc " + results.get(i).docID + " is relevant!");
                int docID = results.get(i).docID;

                // Reseting new docVector
                ListIterator<Double> dvWeights = docVect.weights.listIterator();
                while(dvWeights.hasNext()) {
                    dvWeights.next();
                    dvWeights.set(0.0);
                }

                // Looping through index to build document vector
                for(Map.Entry<String, PostingsList> w: Index.index.entrySet()) {
                    for(PostingsEntry pe: w.getValue().list) {
                        if(pe.docID == docID) {
                            if(docVect.terms.contains(w.getKey())) {
                                Iterator<String> dvT = docVect.terms.iterator();
                                ListIterator<Double> dvW = docVect.weights.listIterator();
                                while(dvT.hasNext()) {
                                    dvW.next();
                                    if(dvT.next().equals(w.getKey())) {
                                        dvW.set(pe.score_tfidf);
                                        break;
                                    }
                                }
                            } else {
                                docVect.terms.add(w.getKey());
                                docVect.weights.add(pe.score_tfidf);
                                this.terms.add(w.getKey());
                                this.weights.add(0.0);
                            }
                        } else if(pe.docID > docID) {
                            break;
                        }
                    }
                }

                // Normalizing docVect:
                docVect.normalize();

                System.out.println("Here is the doc vector:");
                Iterator<String> dvT = docVect.terms.iterator();
                Iterator<Double> dvW = docVect.weights.iterator();
                while(dvT.hasNext()) {
                    System.out.println(dvW.next() + "    " + dvT.next());
                }

                // Adding to previous query:
                ListIterator<Double> docWeights = docVect.weights.listIterator();
                ListIterator<Double> queryWeights  = this.weights.listIterator();
                while(queryWeights.hasNext()) {
                    double d = queryWeights.next();
                    queryWeights.set(d + Query.beta * docWeights.next() / relevantCount);
                }
            }
        }

        System.out.println("Final query, " + this.terms.size() + " elements:");
        Iterator<String> queryTerms = this.terms.iterator();
        Iterator<Double> queryWeights  = this.weights.iterator();
        while(queryTerms.hasNext()) {
            System.out.println(queryWeights.next() + "    " + queryTerms.next());
        }
    }
}

    
