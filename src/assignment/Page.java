package assignment;

import java.net.URL;

/**
 * The assignment.Page class holds anything that the QueryEngine returns to the server.  The field and method
 * we provided here is the bare minimum requirement to be a assignment.Page - feel free to add anything you
 * want as long as you don't break the getURL method.
 *
 */
public class Page {
    // The URL the page was located at.
    private URL url;

    /**
     * Creates a .Page with a given URL.
     * @param url The url of the page.
     */
    public Page(URL url) {
        this.url = url;
    }

    /**
     * @return the URL of the page.
     */
    public URL getURL() {
        return url;
    }
}
