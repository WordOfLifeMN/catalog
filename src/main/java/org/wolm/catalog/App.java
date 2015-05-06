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
import org.wolm.catalog.catalog.BookletsPageRender;
import org.wolm.catalog.catalog.Catalog;
import org.wolm.catalog.catalog.ResourcesPageRender;
import org.wolm.catalog.catalog.SeriesIndexPageRender;
import org.wolm.catalog.environment.RenderEnvironment;
import org.wolm.catalog.environment.TypeFilter;
import org.wolm.catalog.environment.VisibilityFilter;
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

	private RenderEnvironment env = RenderEnvironment.instance();

	private String s3BucketName = "wordoflife.mn.catalog";
	private String s3ObjectPrefix = null;

	private static App instance = null;

	public static App instance() {
		if (instance == null) {
			synchronized (App.class) {
				try {
					if (instance == null) instance = new App();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return instance;
	}

	/** Construct the application */
	private App() throws Exception {
		super();
	}

	public static void main(String[] args) throws Exception {
		App app = instance();

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

		logInfo("Catalog downloading from Google ...");
		Catalog catalog = new Catalog();
		catalog.populateFromGoogleSpreadsheets();

		buildRecentMessages(catalog);
		buildRecentSeries(catalog);
		buildPublicCatalog(catalog);
		buildHandoutsAndResources(catalog);
		buildBooklets(catalog);

		buildCovenantPartnerCatalog(catalog);

		buildCORECatalog(catalog);

		logInfo("Catalog file generation is complete");
	}

	private void buildRecentMessages(Catalog catalog) throws Exception {
		logInfo("Writing recent messages to 'recent-messages.html' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PUBLIC));
		env.addFilter(new TypeFilter().withoutType("C.O.R.E."));

		// find messages
		Series recentMessages = catalog.getRecentMessages(60);
		PageRender pageRender = new SeriesPageRender(recentMessages);
		File outputFile = new File(outputFileDir, "recent-messages.html");
		pageRender.render(outputFile);
	}

	private void buildRecentSeries(Catalog catalog) throws Exception {
		logInfo("Writing recent series to 'recent-series.html' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PUBLIC));
		env.addFilter(new TypeFilter().withoutType("C.O.R.E."));

		// find series
		List<Series> recentSeries = catalog.getRecentSeries(60);
		PageRender pageRender = new SeriesIndexPageRender(recentSeries);
		((SeriesIndexPageRender) pageRender).setIndexTitle("Recent Series from Word of Life Ministries");
		File outputFile = new File(outputFileDir, "recent-series.html");
		pageRender.render(outputFile);
	}

	private void buildPublicCatalog(Catalog catalog) throws Exception {
		logInfo("Writing all public series to 'catalog.html' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PUBLIC));
		env.addFilter(new TypeFilter().withoutType("C.O.R.E."));

		catalog.sortSeriesByDate();
		PageRender pageRender = new SeriesIndexPageRender(catalog.getCompletedSeriesWithStandAloneMessages());
		((SeriesIndexPageRender) pageRender).setIndexTitle("Word of Life Ministries Catalog");
		((SeriesIndexPageRender) pageRender).setIndexDescription(getCatalogIndexDescription());
		File outputFile = new File(outputFileDir, "catalog.html");
		pageRender.render(outputFile);
	}

	private void buildHandoutsAndResources(Catalog catalog) throws Exception {
		logInfo("Writing handouts and resources to 'resources.html' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PUBLIC));
		env.addFilter(new TypeFilter().withoutType("C.O.R.E."));

		List<NamedLink> resources = catalog.getHandoutsAndResources();
		PageRender pageRender = new ResourcesPageRender(resources);
		File outputFile = new File(outputFileDir, "resources.html");
		pageRender.render(outputFile);
	}

	private void buildBooklets(Catalog catalog) throws Exception {
		logInfo("Writing booklets to 'booklets.html' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PUBLIC));
		env.addFilter(new TypeFilter().withoutType("C.O.R.E."));

		List<NamedLink> resources = catalog.getBooklets();
		PageRender pageRender = new BookletsPageRender(resources);
		File outputFile = new File(outputFileDir, "booklets.html");
		pageRender.render(outputFile);
	}

	private void buildCovenantPartnerCatalog(Catalog catalog) throws Exception {
		logInfo("Writing all protected series to 'catalog-cpartner.html' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PROTECTED));
		env.addFilter(new TypeFilter().withoutType("C.O.R.E."));

		catalog.sortSeriesByDate();
		SeriesIndexPageRender pageRender = new SeriesIndexPageRender(catalog.getCompletedSeries());
		pageRender.setIndexTitle("Word of Life Ministries Catalog For Covenant Partners");
		pageRender.setIndexDescription(getCovenantPartnerIndexDescription());
		File outputFile = new File(outputFileDir, "catalog-cpartner.html");
		pageRender.render(outputFile);
	}

	private void buildCORECatalog(Catalog catalog) throws Exception {
		logInfo("Writing all public C.O.R.E. series to 'core.html' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PUBLIC));
		env.addFilter(new TypeFilter().withType("C.O.R.E."));

		catalog.sortSeriesByDate();
		SeriesIndexPageRender pageRender = new SeriesIndexPageRender(catalog.getCompletedSeries());
		pageRender.setIndexTitle("C.O.R.E. Programs");
		pageRender.setIndexDescription(getCoreIndexDescription());
		pageRender.setDepartment("CORE");
		File outputFile = new File(outputFileDir, "core.html");
		pageRender.render(outputFile);
	}

	/** Upload all pages that have been created to S3 (if requested) */
	public void upload() throws Exception {
		// bail if we're not supposed to upload
		if (!isUpload()) return;

		AwsS3Helper s3Helper = AwsS3Helper.instance();
		Bucket catalogBucket = s3Helper.getBucket(getS3BucketName());
		if (catalogBucket == null) throw new Exception("Cannot find the catalog bucket: '" + getS3BucketName() + "'");

		logInfo("Uploading all generated pages to the " + getS3BucketName() + " S3 bucket ...");
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
				logError("Unable to complete upload.");
				e.printStackTrace();
			}
		}
		pool.shutdown();

		logInfo("Uploading complete");
	}

	private String getS3KeyForFile(File file) {
		if (getS3ObjectPrefix() == null) return file.getName();
		return getS3ObjectPrefix() + "/" + file.getName();
	}

	/*
	 * Logging
	 */
	private static String logIndent = "";

	public static void logIndent() {
		logIndent += "  ";
	}

	public static void logOutdent() {
		if (logIndent.length() < 1) return;
		logIndent = logIndent.substring(2);
	}

	public static void logDebug(String msg) {
		if (instance.isVerbose()) System.out.println(logIndent + msg);
	}

	public static void logInfo(String msg) {
		System.out.println(logIndent + msg);
	}

	public static void logWarn(String msg) {
		System.out.println(logIndent + "WARNING: " + msg);
	}

	public static void logError(String msg) {
		System.out.println(logIndent + "ERROR: " + msg);
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

	private String getCatalogIndexDescription() {
		StringBuilder b = new StringBuilder();

		b.append("<table>");
		b.append("  <tr>");
		b.append("    <td><img src='https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/remix.jpeg' width='128'/></td>");
		b.append("    <td>");
		b.append("      <h3>");
		b.append("        We&apos;re currently re-editing and bringing the past 10 years of messages and study materials ");
		b.append("        up to date. New content will be added weekly through the winter of 2014-2015, ");
		b.append("        so check back frequently!");
		b.append("      </h3>");
		b.append("    </td>");
		b.append("  </tr>");
		b.append("</table>");

		return b.toString();
	}

	private String getCovenantPartnerIndexDescription() {
		StringBuilder b = new StringBuilder();

		b.append("<table>");
		b.append("  <tr>");
		b.append("    <td valign=\"top\"><img src='https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/Covenant+Partner+Thumb.jpg' width='164'/></td>");
		b.append("    <td>");
		b.append("      <p style=\"color:maroon;font-weight:bold;\">Please do not share access to this page with anyone.</p>");
		b.append("      <p>Any questions about access to this page should be directed to Pastor Vern or Kevin Murray.</p>");
		b.append("      <p>");
		b.append("        Many of these messages may be rough, unedited, or have other quality problems, ");
		b.append("        and we are not prepared to release them to the public <em>yet</em>. However, there may also be ");
		b.append("        resources on this page that contain sensitive information that we may never choose to ");
		b.append("        release publicly, and we appreciate your discretion as covenant partners.");
		b.append("      </p>");
		b.append("    </td>");
		b.append("  </tr>");
		b.append("</table>");

		return b.toString();
	}

	private String getCoreIndexDescription() {
		StringBuilder b = new StringBuilder();

		b.append("<table>");
		b.append("  <tr>");
		b.append("    <td valign=\"top\"><img src='https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/corestaff.jpg' width='164'/></td>");
		b.append("    <td>");
		b.append("      <p>C.O.R.E.: Center of Our Relationship Experiences</p>");
		b.append("      <p>");
		b.append("        Mary Peltz is a certified counselor with A.A.C.C. and is a Co-Pastor at Word of Life Ministries ");
		b.append("        which is affiliated and licensed through ");
		b.append("        <a href=\"http://www.afcminternational.org\" target=\"_blank\">A.F.C.M. International</a>.");
		b.append("      </p>");
		b.append("      <p>");
		b.append("        Mary specializes in communication skills and restoring relationships and families. ");
		b.append("        She administrates C.O.R.E. programs which is a \"Freedom From\" program that brings help to ");
		b.append("        schools, group homes and staff situations. She is currently facilitating C.O.R.E. Programs at ");
		b.append("        the jails in the Northern Minnesota areas.");
		b.append("      </p>");
		b.append("    </td>");
		b.append("  </tr>");
		b.append("</table>");

		return b.toString();
	}
}
