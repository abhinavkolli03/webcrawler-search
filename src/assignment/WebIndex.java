package assignment;
import java.net.URL;
import java.util.*;


public class WebIndex extends Index{
    private static final long serialVersionUID = 1L;
    // data about the webpages and the indices is stored
    // in this map, and we store the position of each word on a webpage
    // to make sure that we can later check against phrase queries
    protected final Map<String, Map<URL, Set<Long>>> data;
    // set to store and return all the urls found in index and crawling
    protected final Set<URL> urls;
    // counter index for each word found in a document
    protected long counter = 0;
    public WebIndex() {
        data = new HashMap<>();
        urls = new HashSet<>();
    }

    //adds a word, word index, and URL into dataset of WebIndex
    public void append(String word, URL url) {
        // checks to see if the word is contained in the map
        if (!data.containsKey(word)) {
            data.put(word, new HashMap<>());
        }

        // checks to see if the given word has the following url or not
        if (!data.get(word).containsKey(url)) {
            data.get(word).put(url, new HashSet<>());
        }
        // adds current index of word in url through counter
        data.get(word).get(url).add(counter);
        ++counter;

        //add to set of urls
        urls.add(url);
    }

    //returns a set of all the URLs
    public Set<URL> getAllURLs() {
        return urls;
    }

    //returns a set of all the URLs where a specific word is found
    public Set<URL> getWordURLs(String word) {
        //if no word is present in data, then return empty set
        if(data.get(word) == null)
            return new HashSet<>();
        return new HashSet<>(data.get(word).keySet());
    }

    //returns the index set of a specific word in a link
    public Set<Long> getIterations(String word, URL link) {
        //if index set is empty, then we return empty set
        if(data.get(word).get(link) == null)
            return new HashSet<>();
        return new HashSet<>(data.get(word).get(link));
    }

    //checks if a word is present in a specific location of a url
    public boolean checkLocation(String word, URL link, long location) {
        //if word or link is not present then bypasses this
        if(data.get(word) != null && data.get(word).get(link) != null)
            return data.get(word).get(link).contains(location);
        return false;
    }
}