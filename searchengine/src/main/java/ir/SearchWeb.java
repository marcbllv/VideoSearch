/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 *   Second version: Laura Jacquemod, 2015
 */  


package ir;

import java.io.File;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *   Class linking the web interface to the search engine
 */
public class SearchWeb {
	//VARIABLES TO CHOOSE:
	static final double weightPopularity = 0.5;
	static final double alpha = 1;
	static final double beta = 0.5;
	static final double thresholdProbability = -10;
	static final double[] popularityScores ={0.3, 0.3, 0.3};
	static final int distanceFrames = 3;
	//For ranked retrieval
	static final boolean optimization = false;    
	static final double idf_threshold = 0.0001;
	//For additional info (title, description,...)
	static final boolean addition = true;
	static final double weight_addition = 0.2;
	
    static final String LOGOPIC = "Videoquery.png";
    static final String BLANKPIC = "blank.jpg";

    static final String DIRNAME = "myFiles";

    public Indexer indexer = new Indexer();
    private Query query; 
    private PostingsList results; 
    private LinkedList<String> dirNames = new LinkedList<String>();
    private int queryType = Index.INTERSECTION_QUERY;
    private int rankingType = Index.TF_IDF;
    private int structureType = Index.UNIGRAM;
    private int frameType = 1;
    private Object indexLock = new Object();
	public int beingClose = 3;

    public SearchWeb(String query) {
        this.query = new Query(SimpleTokenizer.normalize(query));
    }

    public void index() {
        synchronized ( indexLock ) {
            // If no directory specified: try to recover index from files
            if(dirNames.size() == 0) {
                // Recover the document names and the doc sizes
                try {
                    BufferedReader reader = new BufferedReader(new FileReader("savedindex/__docnames__"));
                    String line;
                    while((line = reader.readLine()) != null) {
                        String[] doc = line.split("\\|");
                        Index.docIDs.put(doc[0], doc[1]);
                    }
                    reader.close();

                    reader = new BufferedReader(new FileReader("savedindex/__doclengths__"));
                    while((line = reader.readLine()) != null) {
                        String[] doc = line.split("\\|");
                        Index.docLengths.put(doc[0], Integer.parseInt(doc[1]));
                    }
                    reader.close();

                    reader = new BufferedReader(new FileReader("savedindex/__doctimeframes__"));
                    while((line = reader.readLine()) != null) {
                        String[] doc = line.split("\\|");
                        Index.docTimeFrame.put(doc[0], Double.parseDouble(doc[1]));
                    }
                    reader.close();
                } catch(Exception e) {
                    System.out.println(e.getMessage());
                }

                // Recover the full index
                FileReader f;
                BufferedReader reader;
                String line;

                for(int i = 0 ; i < HashedIndex.INDEX_SAVE_N_FILES ; i++) {
                    try {
                        f = new FileReader("savedindex/" + i);

                        if(f != null) {
                            reader = new BufferedReader(f);

                            while((line = reader.readLine()) != null) {
                                String[] tokens = line.split("\\|");

                                String[] docs = tokens[1].split(";");
                                PostingsList pl = new PostingsList();

                                for(int p = 0 ; p < docs.length ; p++) {
                                    String[] doc = docs[p].split(":");
                                    String[] offsets = doc[1].split(",");
                                    PostingsEntry pe = new PostingsEntry(Integer.parseInt(doc[0]));

                                    for(int q = 0 ; q < offsets.length ; q++) {
                                        pe.offsets.add(Integer.parseInt(offsets[q]));
                                    }
                                    pl.add(pe);
                                }
                                Index.index.put(tokens[0], pl);
                            }
                        }
                    } catch(FileNotFoundException e) {
                    } catch(IOException e) {
                        System.out.println("IO Error while reading saved index");
                    }
                }
            } else {
                for ( int i=0; i<dirNames.size(); i++ ) {
                    File dokDir = new File( dirNames.get( i ));
                    indexer.processFiles( dokDir );
                }
            }

            // Computing tfidfs
            HashedIndex.computeAllTfidf();

            // Computing champions lists
            HashedIndex.computeChampionsLists();
        }
    }

    public PostingsList search() {
        //return indexer.index.search(query, queryType, rankingType, frameType, weightPopularity, 
        //        popularityScores, distanceFrames, optimization, idf_threshold, addition, weight_addition);
        return indexer.index.search(query, queryType, rankingType, frameType);
    }
}        
