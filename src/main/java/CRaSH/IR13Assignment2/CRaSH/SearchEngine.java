package CRaSH.IR13Assignment2.CRaSH;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.util.Version;

/**
 * 
 */
public class SearchEngine {
	private int fragmentSize = 50;
	private int excerptsLength = 5;
	
	private IndexReader reader;
	private IndexSearcher searcher;
	private QueryParser parser;
	
	/**
	 * Default constructor.
	 */
	public SearchEngine() {
		try {
			reader = DirectoryReader.open(App.directory);
			searcher = new IndexSearcher(reader);
			parser = new QueryParser(Version.LUCENE_46, "text", App.analyzer);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	/**
	 * 
	 * 
	 * @param fragments
	 * @param excerpts
	 */
	public SearchEngine(int fragments, int excerpts) {
		this();
		fragmentSize = fragments;
		excerptsLength = excerpts;
	}
	
	/**
	 * Prints the ten most relevant pages matching the given query.
	 * 
	 * @param query
	 */
	public void searchQuery(String queryString) {
		try {
			Query query = parser.parse(queryString);
			ScoreDoc[] scoreDocs = searcher.search(query, 10).scoreDocs;
			
			for (byte index = 0; index < scoreDocs.length; index++) {
				Document document = searcher.doc(scoreDocs[index].doc);
				
				System.out.print("Rank: " + (index+1));
				System.out.println(" -> Score: " + scoreDocs[index].score);
				System.out.println("Title: " + document.getField("title").stringValue());
				System.out.println("URL: " + document.getField("url").stringValue());
				
				String[] excerpts = getExcerpts(query, "text", document.get("text"));
				for (String excerpt : excerpts) {
					System.out.println(excerpt);
				}
				
				System.out.println();
			}
		} catch (IOException e) {
			System.out.println(e);
		} catch (ParseException e) {
			System.out.println(e);
		} catch (InvalidTokenOffsetsException e) {
			System.out.println(e);
		}
	}
	
	private String[] getExcerpts(Query query, String fieldName, String fieldContent) throws IOException, InvalidTokenOffsetsException {
		TokenStream stream = TokenSources.getTokenStream(fieldName, fieldContent, App.analyzer);
		QueryScorer scorer = new QueryScorer(query, fieldName);
		Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, fragmentSize);
		
		Highlighter highlighter = new Highlighter(scorer);
		highlighter.setTextFragmenter(fragmenter);
		
		return highlighter.getBestFragments(stream, fieldContent, excerptsLength);
	}
}
