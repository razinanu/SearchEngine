package CRaSH.IR13Assignment2.CRaSH;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 
 */
public class WebCrawler {
	private List<String> crawledURLs = new LinkedList<String>();
	private boolean printSteps;
	private int indexedPages;

	private IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, App.analyzer);
	private IndexWriter writer;

	/**
	 * 
	 * 
	 * @param print
	 */
	public WebCrawler(boolean print) {
		printSteps = print;
		try {
			writer = new IndexWriter(App.directory, config);
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	public int getIndexedPages() {
		return indexedPages;
	}
	
	/**
	 * 
	 * 
	 * @param startURL
	 * @param recursionDepth
	 */
	public void startCrawling(String startURL, int recursionDepth) {
		// get all links from the start page
		List<String> remainingURLs = crawl(startURL, recursionDepth);

		while (recursionDepth >= 0) {
			List<String> newURLs = new LinkedList<String>();
			
			// crawl all stored urls and store the new ones in another list
			for (String url : remainingURLs) {
				newURLs.addAll(crawl(url, recursionDepth));
			}
			
			remainingURLs = newURLs;
			recursionDepth--;
		}

		try {
			writer.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * 
	 * 
	 * @param url
	 * @param remainingSteps
	 * 
	 * @return
	 */
	private List<String> crawl(String url, int remainingSteps) {
		if (crawledURLs.contains(url)) {
			return new LinkedList<String>();
		}

		crawledURLs.add(url);
		List<String> nextURLs = new LinkedList<String>();

		try {
			// get the webpage of the current url and index it
			org.jsoup.nodes.Document document = Jsoup.connect(url).get();
			indexPage(url, document.title(), document.body().text());
			indexedPages++;
			
			if (remainingSteps >= 1) {
				// select all links of the current webpage
				Elements links = document.select("a[href]");
				for (Element link : links) {
					String href = link.attr("href");
					if (printSteps) {
						System.out.println("\t" + href);
					}
					nextURLs.add(reconstructURL(url, href));
				}
			}
		} catch (MalformedURLException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.println(e);
		} catch (NullPointerException e) {
			System.out.println(e);
		}

		return nextURLs;
	}
	
	/**
	 * 
	 * @param url
	 * @param href
	 * 
	 * @return
	 * 
	 * @throws MalformedURLException
	 */
	private String reconstructURL(String url, String href) throws MalformedURLException {
		// link to external page, is already complete
		if (href.startsWith("http://")) {
			return href;
		}
		
		// link to mail address or javascript
		if (href.startsWith("mailto:") || href.startsWith("javascript:")) {
			href = "";
		}
		
		if (href.contains("#")) {
			href = href.substring(0, href.indexOf("#"));
		}
		
		// merge current url and incomplete internal link
		return new URL(new URL(url), href).toString();
	}

	/**
	 * 
	 * 
	 * @param url
	 * @param title
	 * @param text
	 */
	private void indexPage(String url, String title, String text) {
		Document document = new Document();

		// store url, title and text of a webpage
		Field urlField = new StringField("url", url, Field.Store.YES);
		document.add(urlField);
		Field titleField = new TextField("title", title, Field.Store.YES);
		document.add(titleField);
		Field textField = new TextField("text", text, Field.Store.YES);
		document.add(textField);

		// add it to the index
		try {
			if (printSteps) {
				System.out.println("adding: " + url);
			}
			writer.addDocument(document);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
