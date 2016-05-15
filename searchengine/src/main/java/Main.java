import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Iterator;

import java.net.URI;
import java.net.URISyntaxException;

import static spark.Spark.*;
import spark.template.freemarker.FreeMarkerEngine;
import spark.ModelAndView;
import static spark.Spark.get;

import com.heroku.sdk.jdbc.DatabaseUrl;

import ir.SearchWeb;
import ir.PostingsList;
import ir.PostingsEntry;
import ir.Index;

public class Main {

    public static void main(String[] args) {
        port(Integer.valueOf(System.getenv("PORT")));
        staticFileLocation("/public");

        get("/search", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            String q = request.queryParams("q");

            if(q != null && !q.equals("")) {
                SearchWeb sw = new SearchWeb(q);
                sw.index();
                PostingsList res = sw.search();

                List<Video> videos = new LinkedList<Video>();
                int i = 0;
                for(Iterator<PostingsEntry> itRes = res.list.iterator() ; itRes.hasNext() && i < 10 ; i++) {
                    PostingsEntry pe = itRes.next();
					//Finding the "BEST" time of the video if there are several 
                    int offs = pe.offsets.getFirst();

					if (pe.offsets.size() > 1) {
						LinkedList<Integer> closeFrames = new LinkedList<Integer>();

						double lengthFrame = 0;
                        try{
                            lengthFrame = Index.docTimeFrame.get("" + pe.docID);
                        } catch(Exception e) {
                            lengthFrame = 0;
                        }
						
						for (int k = 0; k < pe.offsets.size() ; k++){
							int count = 0;
							int timePosition = 0;
                            while (timePosition < pe.offsets.size() && pe.offsets.get(k) + sw.beingClose * lengthFrame > pe.offsets.get(timePosition)) {
                                if (Math.abs(pe.offsets.get(k) - pe.offsets.get(timePosition)) < sw.beingClose * lengthFrame) {
									count++;
								}
								timePosition++;
							}
							closeFrames.add(count);
						}
						
						offs = pe.offsets.get(closeFrames.indexOf(Collections.max(closeFrames)));
					}
					
                    String videoPath = Index.docIDs.get(String.valueOf(pe.docID));
                    String videoName = videoPath.substring(10, videoPath.length() - 2);
                    Video v = new Video(videoName, offs);

                    videos.add(v);
                }

                attributes.put("videos", videos);
                attributes.put("query", q);
                return new ModelAndView(attributes, "response.ftl");
            } else {
                response.redirect("/");
                return null;
            }
        }, new FreeMarkerEngine());

        get("/", (req, res) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("query", "");
            return new ModelAndView(attributes, "index.ftl");
        }, new FreeMarkerEngine());
    }
}
