package assignment;

import java.net.URL;
import java.util.*;


public class WebQueryEngine {
    //instance variables for storing the index and URLs
    private WebIndex crawlerIndex;
    private Set<URL> foundURLs;
    private Set<URL> allURLs;

    //constructor to initialize the engine with index and URLs from index
    public WebQueryEngine(WebIndex index) {
        this.crawlerIndex = index;
        this.foundURLs = new HashSet<URL>(crawlerIndex.getAllURLs());
        this.allURLs = index.getAllURLs();
    }

    //factory constructor for WebQueryEngine
    public static WebQueryEngine fromIndex(WebIndex index) {
        return (new WebQueryEngine(index));
    }

    //method takes a query expression as an argument, parses the query, and returns
    //a list of URL’s to pages that match the query
    public Collection<Page> query(String query) {
        //makes lowercase to ensure query is treated as case-insensitive
        query = query.toLowerCase();
        //resets the URL datasets
        foundURLs = new HashSet<URL>();
        allURLs = new HashSet<URL>(crawlerIndex.getAllURLs());
        //creates parse tree from query
        ParseTree parser = new ParseTree(query);
        //evaluates parse tree and returns set of links
        evaluateTree(parser);
        //converts links to set of pages to be returned
        HashSet<Page> finalPages = new HashSet<>();
        for(URL link : foundURLs) {
            finalPages.add(new Page(link));
        }
        return finalPages;
    }

    //method evaluates the parse tree
    public void evaluateTree(ParseTree tree) {
        //retrieve the root node of the parse tree
        Node currentNode = tree.getRoot();
        //if the node value is null, this means the parsing failed
        //and should return 0 queries
        if(currentNode.val == null)
            return;
        //if the children of root node are null, then this is only one word or phrase
        if(currentNode.left == null && currentNode.right == null) {
            processWordsOnly(currentNode.val);
        }
        //otherwise, this is a parse tree and must be evaluated via recursion
        else {
            evaluateRecurse(currentNode);
        }
    }

    //method used to process queries with one word or phrase
    public void processWordsOnly(String val) {
        //splits into individual words and initializes set of currently found URLs
        String[] words = val.split(" ");
        Set<URL> currentURLs;
        //if this is only one word
        if(words.length == 1) {
            //checks if there is no not operator and collects all URLs associated
            //with word from the index
            if(words[0].indexOf("!") != 0)
                currentURLs = crawlerIndex.getWordURLs(words[0]);
            else {
                //counts number of not operators that precede
                int notCounter = 0;
                while (words[0].indexOf("!") == 0) {
                    words[0] = words[0].substring(1);
                    notCounter++;
                }
                //if odd number of not operators, then we must implement notHandling
                if (notCounter % 2 == 1) {
                    currentURLs = notHandling(crawlerIndex.getWordURLs(words[0]));
                }
                //otherwise, simply collect all URLs associated with word
                else {
                    currentURLs = crawlerIndex.getWordURLs(words[0]);
                }
            }
            foundURLs.addAll(currentURLs);
        }
        //phrase handling
        else {
            //collects first word and finds possible links from index
            String word = words[0];
            Set<URL> possiblePages = crawlerIndex.getWordURLs(word);
            //goes through each possible link
            for(URL link : possiblePages) {
                Set<Long> possibleLocations = crawlerIndex.getIterations(word, link);
                //goes through each index of the first word in the current link
                for(long index : possibleLocations) {
                    //checks if the next word for the current index is the next word
                    //in the phrase until entire phrase is found in location
                    boolean validLocation = true;
                    for(int nextWord = 1; nextWord < words.length; nextWord++) {
                        if(!crawlerIndex.checkLocation(words[nextWord],
                                link, index+nextWord))
                            validLocation = false;
                    }
                    //adds link if phrase found
                    if(validLocation) {
                        foundURLs.add(link);
                        break;
                    }
                }
            }
        }
    }

    //recursive method to recurse through the parse tree
    public String evaluateRecurse(Node current) {
        //base case: if left and right children are null, then word
        //reached and value returned
        if(current.left == null && current.right == null) {
            return current.val;
        }
        //goes down left and right children recursively
        String leftWord = evaluateRecurse(current.left);
        String rightWord = evaluateRecurse(current.right);
        Set<URL> leftWordSet;
        Set<URL> rightWordSet;
        //if no not present in beginning, then collect all URLs for given word
        if(leftWord.indexOf("!") != 0)
            leftWordSet = crawlerIndex.getWordURLs(leftWord);
        //completes the not process handling
        else {
            //counts iterations of not
            int notCounter = 0;
            while(leftWord.indexOf("!") == 0) {
                leftWord = leftWord.substring(1);
                notCounter++;
            }
            //if odd number of nots, then do not handling; otherwise simply call
            //the rest of the URLs present for word
            if(notCounter % 2 == 1) {
                leftWordSet = notHandling(crawlerIndex.getWordURLs(leftWord));
            }
            else {
                leftWordSet = crawlerIndex.getWordURLs(leftWord);
            }
        }

        //similar not handling process as leftWord used on rightWord
        if(rightWord.indexOf("!") != 0)
            rightWordSet = crawlerIndex.getWordURLs(rightWord);
        else {
            int notCounter = 0;
            while(rightWord.indexOf("!") == 0) {
                rightWord = rightWord.substring(1);
                notCounter++;
            }
            if(notCounter % 2 == 1) {
                rightWordSet = notHandling(crawlerIndex.getWordURLs(rightWord));
            }
            else {
                rightWordSet = crawlerIndex.getWordURLs(rightWord);
            }
        }

        //goes through the current node's value
        switch (current.val) {
            case "&":
                //if leftWord or rightWord is blank, then foundURls is result of previous
                //parsed query and must be intersected with foundURLs
                if (leftWord.equals("")) {
                    foundURLs = intersection(foundURLs, rightWordSet);
                }
                else if(rightWord.equals("")) {
                    foundURLs = intersection(foundURLs, leftWordSet);
                }
                //otherwise, intersect left and right sets
                else {
                    foundURLs = intersection(leftWordSet, rightWordSet);
                }
                break;
            case "|":
                //if leftWord or rightWord is blank, then foundURls is result of previous
                //parsed query and must be a union with foundURLs
                if (leftWord.equals("")) {
                    foundURLs = union(foundURLs, rightWordSet);
                }
                else if(rightWord.equals("")) {
                    foundURLs = union(foundURLs, leftWordSet);
                }
                //otherwise, unite left and right sets
                else {
                    foundURLs = union(leftWordSet, rightWordSet);
                }
                break;
        }
        return "";
    }

    //method to intersect the elements between left and right word sets
    public Set<URL> intersection(Set<URL> leftWordSet, Set<URL> rightWordSet) {
        leftWordSet.retainAll(rightWordSet);
        return leftWordSet;
    }

    //method to unite the elements between left and right word sets
    public Set<URL> union(Set<URL> leftWordSet, Set<URL> rightWordSet) {
        leftWordSet.addAll(rightWordSet);
        return leftWordSet;
    }

    //method to handle not operations
    public Set<URL> notHandling(Set<URL> currentSet) {
        //finds the difference between the currentSet and all the URLs present in index
        Set<URL> tempSet = allURLs;
        tempSet.removeAll(currentSet);
        return tempSet;
    }
}