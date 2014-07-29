package org.wolm.catalog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

/**
 * Hello world!
 */
@Parameters(separators = "=")
public class App {
	public static void main(String[] args) throws Exception {
		App app = new App();

		JCommander cmd = new JCommander(app, args);
		if (app.isHelpRequested()) {
			cmd.usage();
			System.exit(0);
		}

		app.catalog();
	}

	/* Command line parameters */
	@Parameter(names = { "-h", "--help" }, description = "This help page.", help = true)
	private boolean helpRequested = false;

	@Parameter(names = { "-v", "--verbose" }, description = "Print more output.")
	private boolean verbose = false;

	@Parameter(names = { "-o", "--out" }, description = "Output file name.")
	private String outputFileName = null;

	/** Construct the application */
	private App() {
		super();
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
		// read the Weebly template page
		URL url = new URL("http://www.wordoflifemn.org/media-catalog.html");
		WeeblyPage weeblyCatalogPage = new WeeblyPage(url);
		weeblyCatalogPage.preparePageForRemoteHosting();

		// create a map of content to substitute
		Map<String, String> content = new HashMap<>();
		content.put("Date", new Date().toString());
		content.put("Title", "Word of Life Ministries Online Catalog");

		// insert the content
		weeblyCatalogPage.substituteVariables(content);

		// output to the designated file
		if (outputFileName == null) weeblyCatalogPage.printPage(System.out);
		else {
			try (PrintStream outStream = new PrintStream(new FileOutputStream(new File(outputFileName)))) {
				weeblyCatalogPage.printPage(outStream);
			}
		}
	}
}
