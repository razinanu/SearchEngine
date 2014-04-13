package CRaSH.IR13Assignment2.CRaSH;

import java.util.Scanner;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class App {
	public static Directory directory = new RAMDirectory();
	public static Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);

	public static void main(String[] args) {
		Scanner consoleReader = new Scanner(System.in);

		System.out.println("Welcome tho the CRaSH searching engine. Before you can start searching, an index have to be created.\n" +
				"You can do this via entering a seed url and a maximum recursion depth in the console. A webcrawler will then create the index.\n" +
				"Be aware that the crawling process may take several minutes, depending on your choosen seed url and recursion depth.\n");
		
		System.out.println("Please enter a seed URL.");
		String startURL = consoleReader.next();
		System.out.println("Please enter the recursion depth. (A non-negative integer.)");
		int recursionDepth = consoleReader.nextInt();
		System.out.println("If you wish to see every crawling step type 'y' else 'n'.");
		boolean printSteps = consoleReader.next().equals("y");

		WebCrawler webCrawler = new WebCrawler(printSteps);
		System.out.println("\nStart crawling.\n");
		webCrawler.startCrawling(startURL, recursionDepth);
		System.out.println("\nCrawling complete. " + webCrawler.getIndexedPages() + " pages were indexed.\n");

		System.out.println("You can now configure the search engine. If you wish to use the default configuration type 'q', else 'c'.");
		SearchEngine searchEngine;
		if (consoleReader.next().equals("q")) {
			searchEngine = new SearchEngine();
		} else {
			System.out.println("Please specify the number of excerpts for the summary of your query results.");
			int excerpts = consoleReader.nextInt();
			System.out.println("Please specify the size of the excerpts in bytes.");
			int fragments = consoleReader.nextInt();
			searchEngine = new SearchEngine(fragments, excerpts);
		}
		
		System.out.println("\nYou can now enter your search queries.");
		System.out.println("To terminate the programm, enter '-terminate' in the console.\n");

		while (true) {
			System.out.println("Please enter a query.");
			String command = consoleReader.next();
			if (command.equals("-terminate")) {
				break;
			} else {
				System.out.println();
				searchEngine.searchQuery(command);
			}
		}
	}
}
