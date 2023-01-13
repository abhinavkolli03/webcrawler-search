package assignment;
import org.attoparser.simple.*;
import java.net.*;
import java.util.*;
public class CrawlingMarkupHandler extends AbstractSimpleMarkupHandler {
    private WebIndex index;

    //objects to keep track of current and new urls
    private URL url;
    private List<URL> newURLs;

    //checkers for style and script tag encounters
    private boolean styleTagEncountered;
    private boolean scriptTagEncountered;

    //instantiates objects
    public CrawlingMarkupHandler() {
        index = new WebIndex();
        newURLs = new ArrayList<>();
        styleTagEncountered = false;
        scriptTagEncountered = false;
    }

    //returns the index of data crawled through
    public Index getIndex() {
        return index;
    }

    //returns any new urls that it finds
    public List<URL> newURLs() {
        return newURLs;
    }

    //sets the current url to the passed in url
    public void setURL(URL link) {
        url = link;
    }

    //unimplemented method that is called whenever the document crawling begins
    public void handleDocumentStart(long startTimeNanos, int line, int col) {
    }

    //unimplemented method that is called whenever the document crawling ends
    public void handleDocumentEnd(long endTimeNanos, long totalTimeNanos, int line, int col) {
    }

    //method called by attoparser whenever an open element tag is encountered
    public void handleOpenElement(String elementName, Map<String, String> attributes, int line, int col) {
        //checks if link has attributes to begin with
        if(attributes != null){
            //checks various cases of href tag and adds the base url along with the
            //appended url to the new set of urls to parse later
            if(attributes.containsKey("href")) {
                try {
                    newURLs.add(new URL(url, cleanURL(attributes.get("href"))));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            else if(attributes.containsKey("HREF")) {
                try {
                    newURLs.add(new URL(url, cleanURL(attributes.get("HREF"))));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }
        //checks if element name is script or style and updates checkers
        if(elementName.equals("script")) {
            scriptTagEncountered = true;
        }
        if(elementName.equals("style")) {
            styleTagEncountered = true;
        }
    }

    //method used to clean the passed url of any unnecessary queries
    public String cleanURL(String link) {
        //removes any content after # or ?
        if(link.contains("#")) {
            return link.substring(0, link.indexOf("#"));
        }
        if(link.contains("?")) {
            return link.substring(0, link.indexOf("?"));
        }
        return link;
    }

    //method called by autoparser whenever a close element tag is encountered
    public void handleCloseElement(String elementName, int line, int col) {
        //checks if element name is script or style and updates checkers
        if(elementName.equals("script")) {
            scriptTagEncountered = false;
        }
        if(elementName.equals("style")) {
            styleTagEncountered = false;
        }
    }

    //method called by autoparser whenever text is encountered within tags
    public void handleText(char[] ch, int start, int length, int line, int col) {
        //skips the text surrounded by script and style tags
        if(scriptTagEncountered || styleTagEncountered)
            return;
        //Stores the current word encountered and a stack to keep track of internal tags
        StringBuffer text = new StringBuffer("");
        //goes through the parse char[] from specified start to end
        for(int i = start; i < start + length; i++) {
            char piece = ch[i];
            //if stack is empty, then actual content found
            String letter = Character.toString(piece);
            if(!(letter.equals(" ") || piece == '\\' || piece == '\n' ||
                    piece == '"' || piece == '\r' || piece == '\t')) {
                //transforms uppercase to lowercase and appends
                if(piece >= 65 && piece <= 90)
                    piece += 32;
                //appends valid characters
                text.append(piece);
            }
            //once word is found, then add to index
            else if(text.length() != 0){
                //remove unnecessary punctuation on ends but preserve punctuation between letters
                while(text.length() != 0 && checkChar(text.toString().charAt(0))) {
                    text.deleteCharAt(0);
                }
                while(text.length() != 0 && checkChar(text.toString().charAt(text.length() - 1))) {
                    text.deleteCharAt(text.length() - 1);
                }
                //if word is still present, then add to index
                if(text.length() != 0) {
                    index.append(text.toString(), url);
                    text.setLength(0);
                }
            }
        }
    }

    //method to check if a character is not a letter, number, or dash symbol
    public boolean checkChar(char ch) {
        return (ch < 65 || ch > 90) && (ch < 48 || ch > 57) &&
                (ch < 97 || ch > 122) && (ch != 45);
    }
}