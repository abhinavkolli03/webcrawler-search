package assignment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.net.*;
import java.util.*;
public class IndexCrawlerTest{
    @Test
    public void insertTest() throws IOException {
        //rigor testing the harness
        for(int times = 0; times != 1500; ++times){
            //creates an instance of the crawler and makes it crawl across the created web
            htmlCreator();
            String[] args = {"page1.html"};
            IndexingHarness test = new IndexingHarness(args, times + 1);
            //compares the map gotten from the crawler against the map generated at html document creation
            Map<String, Map<URL, Set<Long>>> data = test.getData();
            for(int i = 1; i != 101; ++i){
                //reads in the HTML files and creates a string of words that need to be checked
                BufferedReader br = new BufferedReader(new FileReader("page" + i + ".html"));
                String reading = br.readLine();
                while(!reading.startsWith("\t<p>")){
                    reading = br.readLine();
                }
                String[] words = reading.substring(4, reading.length() - 4).split(" ");
                long begin = 0;
                //done to make sure that the indexing of the words is correctly ascribed
                Map<URL, Set<Long>> urls = data.get(words[0]);
                boolean detected = false;
                try{
                    for(URL u: data.get(words[0]).keySet()){
                        if(u.toString().equals("page" + i +".html")){
                            long min = Long.MAX_VALUE;
                            for(Long index: urls.get(u))
                                min = Long.min(index, min);
                            begin = min;
                        }
                    }
                }catch(NullPointerException e){detected = true;}
                //checks to make sure the words appear in order throughout the web index
                for(int j = 1; j != words.length; ++j){
                    ++begin;
                    try{
                        for(URL u: data.get(words[j]).keySet()){
                            if(u.toString().equals("page" + i +".html")){
                                Assertions.assertTrue(detected || data.get(words[j]).get(u).contains(begin));
                            }
                        }
                    }catch(NullPointerException e){detected = true;}
                }
            }
        }
    }

    //method used to simulate a portion of the web using HTML documents
    public void htmlCreator() throws IOException {
        //writes random word strings using a words.txt document provided in prog6
        BufferedReader br = new BufferedReader(new FileReader("words.txt"));
        List<String> words = new ArrayList<>();
        for(int i = 0; i != 113764; ++i)
            words.add(br.readLine());
        for(int i = 1; i != 101; ++i){
            PrintWriter pw = new PrintWriter(new FileWriter("page" + i + ".html"));
            pw.println("""
                    <!DOCTYPE html>
                    <html>
                    <head>""");
            pw.println("\t<title>Page" + i + "</title>");
            pw.println("""
                    </head>
                    <body>""");
            pw.println("\t<h1>Page" + i + "</h1>");
            int random = (int)(Math.random() * 101);
            for(int j = 1; j != random + 1; ++j){
                int r = (int)(Math.random() * 100) + 1;
                pw.println("\t<a href=\"page" + r + ".html\">Page" + r + "</a>");
            }
            pw.print("\t<p>");
            int rand = (int)(Math.random() * 901) + 100;
            for(int j = 0; j != rand; ++j){
                String word = words.get((int)(Math.random() * words.size()));
                pw.print(word + " ");
            }
            pw.print("""
                    </p>
                    </body>
                    </html>""");
            pw.close();
        }

    }
}