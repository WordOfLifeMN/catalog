package org.wolm.catalog;

import java.io.File;
import java.io.IOException;

import org.wolm.catalog.catalog.Catalog;
import org.wolm.message.Message;
import org.wolm.series.Series;
import org.wolm.series.SeriesPageRender;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Hello world!
 */
@Parameters(separators = "=")
public class App {
	/* Command line parameters */
	@Parameter(names = { "-h", "--help" }, description = "This help page.", help = true)
	private boolean helpRequested = false;

	@Parameter(names = { "-v", "--verbose" }, description = "Print more output.")
	private boolean verbose = false;

	@Parameter(names = { "-o", "--out" }, description = "Output file directory.")
	private String outputFileDir = null;

	/** Construct the application */
	private App() throws Exception {
		super();
	}

	public static void main(String[] args) throws Exception {
		App app = new App();

		JCommander cmd = new JCommander(app, args);
		if (app.isHelpRequested()) {
			cmd.usage();
			System.exit(0);
		}

		app.catalog();
	}

	public boolean isHelpRequested() {
		return helpRequested;
	}

	public void setHelpRequested(boolean help) {
		this.helpRequested = help;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Generates the catalog of Word of Life Ministries media
	 * 
	 * @throws IOException
	 */
	public void catalog() throws Exception {
		PageRender pageRender;
		File outputFile;

		System.out.println("Catalog downloading from Google…");
		Catalog catalog = new Catalog();
		catalog.init();

		// generate the recent-messages list and save it to a file
		Series recentMessages = catalog.getRecentMessages(60, Message.Visibility.PUBLIC);
		pageRender = new SeriesPageRender(recentMessages);
		outputFile = new File(outputFileDir, "recent-messages.html");
		pageRender.render(outputFile);
		System.out.println("Recent messages complete at " + outputFile);

		// // generate the catalog index and save it to a file
		// catalog.sortSeriesByDate();
		// pageRender = new CatalogSeriesIndexPageRender(catalog);
		// outputFile = new File(outputFileDir, "series.html");
		// pageRender.render(outputFile);
		// System.out.println("Catalog complete at " + outputFile);
	}
}
