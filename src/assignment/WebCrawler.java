package assignment;
import org.attoparser.config.ParseConfiguration;
import org.attoparser.simple.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class WebCrawler {
    public static void main(String[] args) {
        // Basic usage information
        if (args.length < 1) {
            System.err.println("Error: No URLs specified.");
            System.exit(1);
        }

        // We'll throw all the args into a queue for processing.
        Queue<URL> remaining = new LinkedList<>();

        //list to keep track of urls that have already been encountered
        Set<URL> encounteredURLs = new HashSet<>();

        for (String url : args) {
            //adds each link to remaining and encountered lists only if it is a html link
            try {
                if(url.endsWith(".html")){
                    URL link = new URL(url);
                    remaining.add(link);
                    encounteredURLs.add(link);
                }
            } catch(MalformedURLException|NullPointerException e) {
                // Throw this one out!
                System.err.printf("Error: URL '%s' was malformed and will be ignored!%n", url);
            }
        }

        // Create a parser from the attoparser library, and our handler for markup.
        ISimpleMarkupParser parser = new SimpleMarkupParser(ParseConfiguration.htmlConfiguration());
        CrawlingMarkupHandler handler = new CrawlingMarkupHandler();

        // Try to start crawling, adding new URLS as we see them.
        try {
            while (!remaining.isEmpty()) {
                URL currentLink = remaining.poll();
                //passes the url to the CrawlingMarkupHandler object to use as variable
                handler.setURL(currentLink);
                // Parse the next URL's page
                try {
                    parser.parse(new InputStreamReader(currentLink.openStream()), handler);
                } catch(Exception ignored) {
                }
                // Add any new URLs
                for(URL link : handler.newURLs()) {
                    //if link is not already encountered, then add to remaining and encountered
                    boolean html;
                    try{
                        html = link.toString().endsWith(".html");
                        if(!encounteredURLs.contains(link) && html) {
                            remaining.add(link);
                            encounteredURLs.add(link);
                        }
                    }catch(NullPointerException ignored){}
                }
            }

            handler.getIndex().save("index.db");
        } catch (Exception e) {
            // Bad exception handling :(
            System.err.println("Error: assignment.Index generation failed!");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
