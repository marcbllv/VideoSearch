package ir;

import java.util.ArrayList;

public class IntersectionQuery {

    /**
     * Returns the list of common PostingsEntry in the several PostingsLists from lists
     * The PostingsEntry returned are from a list whose index in lists is listIdxReturn
     */
    public static PostingsList intersectResponses(ArrayList<PostingsList> lists, int queryType) {
        int s = lists.size();
        int[] idx = new int[s];
        boolean end = false;
        boolean commonDoc;

        for(int i = 0 ; i < s ; ++i) {
            idx[i] = 0;
        }

        PostingsList intersList = new PostingsList();

        while(!end) {
            // Stop case: if one index > size
            for(int i = 0 ; i < s ; i++) {
                if(idx[i] >= lists.get(i).size()) {
                    end = true;
                }
            }

            if(end)
                break;

            commonDoc = true;
            int sma_idx = 0;
            int sma_val = lists.get(0).get(idx[0]).docID;

            // Checking if same doc for current indices
            // And also recording the smallest value, whose index could be to increment 
            for(int i = 0 ; i < s ; i++) {
                commonDoc &= lists.get(0).get(idx[0]).docID == lists.get(i).get(idx[i]).docID;

                if(sma_val > lists.get(i).get(idx[i]).docID) {
                    sma_val = lists.get(i).get(idx[i]).docID;
                    sma_idx = i;
                }
            }

            // If doc found: increase all indices
            if(commonDoc) {

                switch(queryType) {
                    case Index.PHRASE_QUERY:
                        ArrayList<PostingsEntry> testMatch = new ArrayList<PostingsEntry>(s);
                        for(int i = 0 ; i < s ; i++) {
                            testMatch.add(lists.get(i).get(idx[i]));
                        }
                        if(IntersectionQuery.offsetMatch(testMatch)) {
                            intersList.add(testMatch.get(0));
                        }
                        break;
                    case Index.INTERSECTION_QUERY:
                        intersList.add(lists.get(0).get(idx[0]));
                        break;
                }

                for(int i = 0 ; i < s ; i++) {
                    idx[i]++;
                }
            } else { // Else: increase smallest index
                idx[sma_idx]++;
            }
        }

        return intersList;
    }

    /**
     * This function returns true of false whether offsets match in the postingEntries
     */
    private static boolean offsetMatch(ArrayList<PostingsEntry> postingEntries) {
        int s = postingEntries.size();

        // If only one postingEntry, return it:
        if(s == 1) {
            return true;
        }

        int[] idx = new int[s];
        boolean end = false;
        boolean matching = true;

        while(!end) {
            // Stop condition:
            for(int i = 0 ; i < s ; i++) {
                if(idx[i] >= postingEntries.get(i).size()) {
                    end = true;
                }
            }
            
            if(end)
                break;

            matching = true;
            // If next postingEntry has offset +1, then match
            // Also recording the smallest offset that could be increased
            int sma_idx = 0;
            int sma_val = postingEntries.get(0).get(idx[0]);
            for(int i = 0 ; i < s-1 ; i++) {
                matching &= (postingEntries.get(i).get(idx[i]) == postingEntries.get(i + 1).get(idx[i + 1]) - 1);

                if(sma_val > postingEntries.get(i).get(idx[i])) {
                    sma_val = postingEntries.get(i).get(idx[i]);
                    sma_idx = i;
                }
            }
            if(sma_val > postingEntries.get(s-1).get(idx[s-1])) {
                sma_val = postingEntries.get(s-1).get(idx[s-1]);
                sma_idx = s-1;
            }

            // If match return true, else increase only the smallest index
            if(matching) {
                return true;
            } else {
                idx[sma_idx]++;
            }
        }

        return false;
    }
}
