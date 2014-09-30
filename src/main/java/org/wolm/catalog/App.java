package org.wolm.catalog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;

import org.wolm.catalog.catalog.Catalog;
import org.wolm.catalog.catalog.CatalogSeriesIndexPageRender;
import org.wolm.series.Series;
import org.wolm.series.SeriesPageRender;
import org.wolm.series.SeriesUrlRender;
import org.wolm.weebly.WeeblyPage;

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

	@Parameter(names = { "-o", "--out" }, description = "Output file name.")
	private String outputFileName = null;

	/** URL to the template used to create the pages */
	private final URL pageTemplateUrl;

	/** Construct the application */
	private App() throws Exception {
		super();
		pageTemplateUrl = new URL("http://www.wordoflifemn.org/media-catalog.html");
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
		// read the Weebly template page
		Catalog catalog = new Catalog();
		catalog.init();

		// generate the catalog index and save it to the output file
		System.out.println("Generating series index ... ");
		catalog.sortSeriesByDate();
		CatalogSeriesIndexPageRender indexRender = new CatalogSeriesIndexPageRender(pageTemplateUrl, catalog);
		WeeblyPage weeblyCatalogPage = indexRender.render();
		try (PrintStream outStream = new PrintStream(new FileOutputStream(new File(outputFileName)))) {
			weeblyCatalogPage.printPage(outStream);
		}

		// for each series, generate the series page and save to the output directory
		File outputDirectory = new File(outputFileName).getParentFile();
		for (Series series : catalog.getSeries()) {
			System.out.println("  Generating series: " + series.getTitle() + " ... ");
			SeriesPageRender seriesRender = new SeriesPageRender(pageTemplateUrl, series);
			WeeblyPage seriesPage = seriesRender.render();
			File outputFile = new File(outputDirectory, new SeriesUrlRender(series).getFileName());
			try (PrintStream outStream = new PrintStream(new FileOutputStream(outputFile))) {
				seriesPage.printPage(outStream);
			}
		}
	}
}
