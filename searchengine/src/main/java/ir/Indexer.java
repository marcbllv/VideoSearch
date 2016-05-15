/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  


package ir;

import java.io.File;
import java.io.Reader;
import java.io.FileReader;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.LinkedList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *   Processes a directory structure and indexes all PDF and text files.
 */
public class Indexer {

    /** The index to be built up by this indexer. */
    public Index index;

    public static final double THRESHOLD_PROBA = -10.0;

    /** The next docID to be generated. */
    private int lastDocID = 0;

    /** Generates a new document identifier as an integer. */
    private int generateDocID() {
        return lastDocID++;
    }

    /** Generates a new document identifier based on the file name. */
    private int generateDocID( String s ) {
        return s.hashCode();
    }


    /* ----------------------------------------------- */


    /**
     *  Initializes the index as a HashedIndex.
     */
    public Indexer() {
        index = new HashedIndex();
    }


    /* ----------------------------------------------- */


    /**
     *  Tokenizes and indexes the file @code{f}. If @code{f} is a directory,
     *  all its files and subdirectories are recursively processed.
     */
    public void processFiles( File f ) {
        // do not try to index fs that cannot be read
        if ( f.canRead() ) {
            if ( f.isDirectory() ) {
                String[] fs = f.list();
                // an IO error could occur
                if ( fs != null ) {
                    for ( int i=0; i<fs.length; i++ ) {
                        processFiles( new File( f, fs[i] ));
                    }
                }
            } else {
                // First register the document and get a docID
                int docID = generateDocID();
                Index.docIDs.put( "" + docID, f.getPath() );
                try {
                    int length = 0;

					/***************************************************
					 *      GETTING INFORMATION FROM JSON FILE         *
					 *              (file with metadata)               *
					 **************************************************/
					//Getting the name of the file
                    String file = "Metadata/" + f.getName().substring(0, f.getName().length() - 2) + "_m";

					JSONParser parser = new JSONParser();
					
					Object obj = parser.parse(new FileReader(file));
					JSONObject jsonObject = (JSONObject) obj;

					JSONArray items = (JSONArray) jsonObject.get("items");

					JSONObject item = (JSONObject) items.get(0);
					JSONObject information = (JSONObject) item.get("snippet");
					
					String title = (String) information.get("title");
					String descr = title + " " + (String) information.get("description");
					
					Reader reader = new StringReader(descr);
					
					SimpleTokenizer tok = new SimpleTokenizer( reader );
					int tokens = 0;
					while ( tok.hasMoreTokens() ) {
						String token = tok.nextToken();
						//Adding additional information to frame "-1"
						insertIntoIndex(docID, token, -1);
						tokens++;
					}
					length += tokens;
					reader.close();
					
					
					/***************************************************
					 *      GETTING INFORMATION FROM JSON FILE         *
					 *            (file with description)              *
					 **************************************************/
					
					obj = parser.parse(new FileReader(f));
					jsonObject = (JSONObject) obj;

					JSONArray listFrames = (JSONArray) jsonObject.get("imgblobs");
					
					for (int i = 0; i < listFrames.size(); ++i) {
						//Getting the information concerning the frame
						JSONObject frame = (JSONObject) listFrames.get(i);
						
						JSONObject candidate = (JSONObject) frame.get("candidate");
						double probability = (double) candidate.get("logprob");
						
						if (i == 1) {
							//Taking the amount of time between two frames
							//At i=0, t =0, so we have to look at second frame
							Double time = (Double) frame.get("time [s]");
							int timeFrame = time.intValue();
                            Index.docTimeFrame.put( "" + docID, time);
						}
						
						if (probability > Indexer.THRESHOLD_PROBA) {
							//If it is sure enough, we had info to index
							String description = (String) candidate.get("text");
							reader = new StringReader(description);
							Double time = (Double) frame.get("time [s]");
							int Nbframe = time.intValue();
							
							//Normal description
							//tok = new SimpleTokenizer( reader );
							//tokens = 0;
							//while ( tok.hasMoreTokens() ) {
							//	String token = tok.nextToken();
							//	insertIntoIndex(docID, token, Nbframe);
							//	tokens++;
							//}
							//
							//length += tokens;
							//reader.close();
							
							//Extended description
							String extended = (String) frame.get("extended");
							Reader readerLong = new StringReader(extended);
							tok = new SimpleTokenizer( readerLong );
							tokens = 0;
							while ( tok.hasMoreTokens() ) {
								String token = tok.nextToken();
								insertIntoIndex(docID, token, Nbframe);
								tokens++;
							}
							length += tokens;
							reader.close();
						}
					}
					Index.docLengths.put( "" + docID, length );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     *  Indexes one token.
     */
    public void insertIntoIndex( int docID, String token, int offset ) {
        index.insert( token, docID, offset );
    }

    public void scanFilesTrue() {
        ((HashedIndex)this.index).scanFilesTrue();
    }

}

