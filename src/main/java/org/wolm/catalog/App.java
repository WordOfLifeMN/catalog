package org.wolm.catalog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.wolm.aws.AwsS3Helper;
import org.wolm.catalog.catalog.Catalog;
import org.wolm.catalog.catalog.ResourcesPageRender;
import org.wolm.catalog.catalog.SeriesIndexPageRender;
import org.wolm.series.Series;
import org.wolm.series.SeriesPageRender;

import com.amazonaws.services.s3.model.Bucket;
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

	private String s3BucketName = "wordoflife.mn.catalog";
	private String s3ObjectPrefix = null;

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

		app.upload();
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
			RenderFactory.setBaseRef("http://s3-us-west-2.amazonaws.com/" + getS3BucketName()
					+ (getS3ObjectPrefix() == null ? "" : "/" + getS3ObjectPrefix()));
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
		catalog.populateFromGoogleSpreadsheets();

		// generate the recent-messages list and save it to a file
		{
			System.out.println("Writing series to file 'recent-messages.html'…");
			Series recentMessages = catalog.getRecentMessages(60);
			PageRender pageRender = new SeriesPageRender(recentMessages);
			File outputFile = new File(outputFileDir, "recent-messages.html");
			pageRender.render(outputFile);
		}

		// generate the recent-series list and save it to a file
		{
			List<Series> recentSeries = catalog.getRecentSeries(60);
			PageRender pageRender = new SeriesIndexPageRender(recentSeries);
			((SeriesIndexPageRender) pageRender).setIndexTitle("Recent Series from Word of Life Ministries");
			File outputFile = new File(outputFileDir, "recent-series.html");
			pageRender.render(outputFile);
		}

		// generate the catalog index and save it to a file
		{
			catalog.sortSeriesByDate();
			PageRender pageRender = new SeriesIndexPageRender(catalog.getCompletedSeriesWithStandAloneMessages());
			((SeriesIndexPageRender) pageRender).setIndexTitle("Word of Life Ministries Catalog");
			((SeriesIndexPageRender) pageRender)
					.setIndexDescription("<table><tr>"
							+ "<td><img src='https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/remix.jpeg' width='128'/></td>"
							+ "<td><h3>We&apos;re currently re-editing and bringing the past 10 years "
							+ "of messages and study materials up to date. "
							+ "New content will be added weekly through the winter of 2014-2015, so check back frequently!</h3>"
							+ "</td></table>");
			File outputFile = new File(outputFileDir, "catalog.html");
			pageRender.render(outputFile);
		}

		// generate the resource list and save it to a file
		{
			List<NamedLink> resources = catalog.getResources();
			PageRender pageRender = new ResourcesPageRender(resources);
			File outputFile = new File(outputFileDir, "resources.html");
			pageRender.render(outputFile);
		}

		System.out.println("Catalog file generation is complete");
	}

	/** Upload all pages that have been created to S3 (if requested) */
	public void upload() throws Exception {
		// bail if we're not supposed to upload
		if (!isUpload()) return;

		AwsS3Helper s3Helper = AwsS3Helper.instance();
		Bucket catalogBucket = s3Helper.getBucket(getS3BucketName());
		if (catalogBucket == null) throw new Exception("Cannot find the catalog bucket: '" + getS3BucketName() + "'");

		System.out.println("Uploading all generated pages to the " + getS3BucketName() + " S3 bucket…");
		List<Future<Boolean>> futures = new ArrayList<>();
		ExecutorService pool = Executors.newFixedThreadPool(8);
		for (File page : RenderFactory.getCreatedPages()) {
			// new UploadFileToS3Callable(catalogBucket, getS3KeyForFile(page), page).call();
			futures.add(pool.submit(new UploadFileToS3Callable(catalogBucket, getS3KeyForFile(page), page)));
		}
		for (Future<Boolean> future : futures) {
			try {
				future.get();
			}
			catch (Exception e) {
				System.out.println("Unable to complete upload.");
				e.printStackTrace();
			}
		}
		pool.shutdown();

		System.out.println("Uploading complete");
	}

	private String getS3KeyForFile(File file) {
		if (getS3ObjectPrefix() == null) return file.getName();
		return getS3ObjectPrefix() + "/" + file.getName();
	}

	/**
	 * Uploads a file to a specific S3 bucket and object key
	 * 
	 * @author wolm
	 */
	private static class UploadFileToS3Callable implements Callable<Boolean> {
		static final AwsS3Helper s3Helper = AwsS3Helper.instance();
		final Bucket bucket;
		final String key;
		final File file;

		public UploadFileToS3Callable(Bucket bucket, String key, File file) {
			super();
			this.bucket = bucket;
			this.key = key;
			this.file = file;
		}

		public Boolean call() throws Exception {
			System.out.println("  Uploading page: " + file);
			s3Helper.uploadPublicFile(bucket, key, file);
			return Boolean.TRUE;
		}

	}
}
