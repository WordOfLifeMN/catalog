package org.wolm.catalog;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.wolm.catalog.catalog.Catalog;
import org.wolm.catalog.catalog.SeriesIndexPageRender;
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

	@Parameter(names = { "-u", "--upload" }, description = "Upload final files to S3.")
	private boolean upload = false;

	private String s3BucketName = "wordoflife.mn.audio";
	private String s3ObjectPrefix = "etc";

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

		app.init();

		app.catalog();
	}

	private void init() {
		initRenderFactory();
	}

	/**
	 * Sets up the global render factory settings based on the configuration options
	 * 
	 */
	private void initRenderFactory() {
		if (isUpload()) {
			// uploading to S3: the baseref is the URL to the s3 bucket
			RenderFactory.setBaseRef("https://s3-us-west-2.amazonaws.com/" + getS3BucketName() + "/"
					+ getS3ObjectPrefix());
		}
		else {
			// not uploading to S3: the baseref is the file directory we are outputting to
			RenderFactory.setBaseRef("file://" + outputFileDir.toString());
		}

		RenderFactory.setMinVisibility(AccessLevel.PUBLIC);
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

	public boolean isUpload() {
		return upload;
	}

	public void setUpload(boolean upload) {
		this.upload = upload;
	}

	public String getS3BucketName() {
		return s3BucketName;
	}

	public void setS3BucketName(String s3BucketName) {
		this.s3BucketName = s3BucketName;
	}

	public String getS3ObjectPrefix() {
		return s3ObjectPrefix;
	}

	public void setS3ObjectPrefix(String s3ObjectPrefix) {
		this.s3ObjectPrefix = s3ObjectPrefix;
	}

	/**
	 * Generates the catalog of Word of Life Ministries media
	 * 
	 * @throws IOException
	 */
	public void catalog() throws Exception {

		System.out.println("Catalog downloading from Google…");
		Catalog catalog = new Catalog();
		catalog.init();

		// generate the recent-messages list and save it to a file
		{
			Series recentMessages = catalog.getRecentMessages(60);
			PageRender pageRender = new SeriesPageRender(recentMessages);
			File outputFile = new File(outputFileDir, "recent-messages.html");
			pageRender.render(outputFile);
			System.out.println("Recent messages complete at " + outputFile);
		}

		// generate the recent-serieslist and save it to a file
		{
			List<Series> recentSeries = catalog.getRecentSeries(60);
			PageRender pageRender = new SeriesIndexPageRender(recentSeries);
			((SeriesIndexPageRender) pageRender).setIndexTitle("Recent Series from Word of Life Ministries");
			File outputFile = new File(outputFileDir, "recent-series.html");
			pageRender.render(outputFile);
			System.out.println("Recent series complete at " + outputFile);
		}

		// generate the catalog index and save it to a file
		{
			catalog.sortSeriesByDate();
			PageRender pageRender = new SeriesIndexPageRender(catalog.getCompletedSeries());
			((SeriesIndexPageRender) pageRender).setIndexTitle("Word of Life Ministries Catalog");
			File outputFile = new File(outputFileDir, "catalog.html");
			pageRender.render(outputFile);
			System.out.println("Catalog complete at " + outputFile);
		}
	}
}
