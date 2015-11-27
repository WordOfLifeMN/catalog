package org.wolm.catalog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
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
import org.wolm.catalog.catalog.SeriesIndexWithPromoPageRender;
import org.wolm.catalog.environment.BookletFilter;
import org.wolm.catalog.environment.EntirelyWithinYearFilter;
import org.wolm.catalog.environment.IntersectingWithYearFilter;
import org.wolm.catalog.environment.RecentFilter;
import org.wolm.catalog.environment.RenderEnvironment;
import org.wolm.catalog.environment.StartedWithinYearFilter;
import org.wolm.catalog.environment.TypeFilter;
import org.wolm.catalog.environment.VisibilityFilter;
import org.wolm.message.Message;
import org.wolm.series.Series;
import org.wolm.series.SeriesHelper;
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

	/** Policy to define series inclusion with a date range (like a year) */
	private enum InclusionPolicy {
		/** Include any series that has a message in the date range */
		intersectingWith,
		/** Include only series that started in the date range, regardless of completion date */
		startedWithin,
		/** Include any series that started in the date range AND (finished in the date range OR hasn't finished yet) */
		entirelyWithin
	}

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
		buildSeriesForYear(catalog, 2015, InclusionPolicy.startedWithin);
		buildSeriesForYear(catalog, 2015, InclusionPolicy.intersectingWith);
		buildPublicCatalog(catalog);
		buildHandoutsAndResources(catalog);
		buildBooklets(catalog);

		buildCovenantPartnerCatalog(catalog);

		buildCORECatalog(catalog);
		buildAskPastorCatalog(catalog);

		logInfo("Catalog file generation is complete");
	}

	private void buildRecentMessages(Catalog catalog) throws Exception {
		logInfo("Writing recent messages to 'recent-messages.html' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PUBLIC));
		env.addFilter(new TypeFilter().withoutType("C.O.R.E."));
		env.addFilter(new RecentFilter().withDays(60));
		env.addFilter(new BookletFilter(false));

		// find messages
		Series recentMessages = catalog.getFilteredMessagesInASeries();
		recentMessages.setTitle("Recent Messages from Word of Life Ministries");
		recentMessages.setDescription("Recent messages from the last " + 60 + " days.");
		recentMessages.sortMessages(Message.byDateDescending);

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
		env.addFilter(new RecentFilter().withDays(60));
		env.addFilter(new BookletFilter(false));

		// find series
		List<Series> series = catalog.getFilteredSeries();
		series.addAll(catalog.getStandAloneMessagesInSeriesByMessage());
		Collections.sort(series, Series.byDate);

		// build the page renderer
		PageRender pageRender = new SeriesIndexWithPromoPageRender(series);
		pageRender.setTitle("Recent Series from Word of Life Ministries");
		addCurrentSeriesPromo(catalog, (SeriesIndexWithPromoPageRender) pageRender);
		File outputFile = new File(outputFileDir, "recent-series.html");
		pageRender.render(outputFile);
	}

	/**
	 * Gets series for a specific year.
	 * 
	 * @param catalog Source catalog
	 * @param year Year to extract series for
	 * @param inclusion Determines how to select whether a series is in a year or not
	 * @throws Exception
	 */
	private void buildSeriesForYear(Catalog catalog, int year, InclusionPolicy inclusion) throws Exception {
		logInfo("Writing " + year + " series to '" + inclusion + year + "-series.html' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PUBLIC));
		env.addFilter(new TypeFilter().withoutType("C.O.R.E."));
		switch (inclusion) {
		case entirelyWithin:
			env.addFilter(new EntirelyWithinYearFilter(year));
			break;
		case intersectingWith:
			env.addFilter(new IntersectingWithYearFilter(year));
			break;
		case startedWithin:
			env.addFilter(new StartedWithinYearFilter(year));
			break;
		}

		// find series
		List<Series> series = catalog.getFilteredSeries();
		series.addAll(catalog.getStandAloneMessagesInSeriesByMessage());
		Collections.sort(series, Series.byDate);

		PageRender pageRender = new SeriesIndexWithPromoPageRender(series);
		pageRender.setTitle("Messages and Series from Word of Life Ministries in " + year);
		if (year == new GregorianCalendar().get(Calendar.YEAR)) {
			addCurrentSeriesPromo(catalog, (SeriesIndexWithPromoPageRender) pageRender);
		}
		File outputFile = new File(outputFileDir, inclusion.toString() + year + "-series.html");
		pageRender.render(outputFile);
	}

	private void buildPublicCatalog(Catalog catalog) throws Exception {
		logInfo("Writing all public series to 'catalog.html' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PUBLIC));
		env.addFilter(new TypeFilter().withoutType("C.O.R.E.", "Ask Pastor"));

		// get all completed and in-progress series plus all stand-alone messages
		List<Series> catalogSeries = catalog.getCompletedSeries();
		catalogSeries.addAll(catalog.getInProgressSeries());
		// catalogSeries.addAll(catalog.getStandAloneMessagesInSeriesByYear());
		catalogSeries.addAll(catalog.getStandAloneMessagesInSeriesByMessage());
		catalogSeries = SeriesHelper.withoutDuplicates(catalogSeries);
		Collections.sort(catalogSeries, Series.byDate);

		PageRender pageRender = new SeriesIndexPageRender(catalogSeries);
		pageRender.setTitle("Word of Life Ministries Catalog");
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
		pageRender.setTitle("Handouts and Resources");
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
		pageRender.setTitle("Booklets");
		File outputFile = new File(outputFileDir, "booklets.html");
		pageRender.render(outputFile);
	}

	private void buildCovenantPartnerCatalog(Catalog catalog) throws Exception {
		logInfo("Writing all protected series to 'catalog-cpartner.html' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PROTECTED));
		env.addFilter(new TypeFilter().withoutType("C.O.R.E.", "Ask Pastor"));

		// get all completed and in-progress series
		List<Series> catalogSeries = catalog.getCompletedSeries();
		catalogSeries.addAll(catalog.getInProgressSeries());
		// catalogSeries.addAll(catalog.getStandAloneMessagesInSeriesByYear());
		catalogSeries.addAll(catalog.getStandAloneMessagesInSeriesByMessage());
		catalogSeries = SeriesHelper.withoutDuplicates(catalogSeries);
		Collections.sort(catalogSeries, Series.byDate);

		SeriesIndexPageRender pageRender = new SeriesIndexPageRender(catalogSeries);
		pageRender.setTitle("Covenant Partner Catalog");
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

		// get all completed and in-progress series
		List<Series> coreSeries = catalog.getCompletedSeries();
		coreSeries.addAll(catalog.getInProgressSeries());
		coreSeries.addAll(catalog.getStandAloneMessagesInSeriesByMessage());
		coreSeries = SeriesHelper.withoutDuplicates(coreSeries);
		Collections.sort(coreSeries, Series.byDate);

		// build the HTML page
		SeriesIndexPageRender pageRender = new SeriesIndexPageRender(coreSeries);
		pageRender.setTitle("C.O.R.E. Programs");
		pageRender.setIndexDescription(getCoreIndexDescription());
		pageRender.setDepartment("CORE");
		File outputFile = new File(outputFileDir, "core.html");
		pageRender.render(outputFile);
	}

	private void buildAskPastorCatalog(Catalog catalog) throws Exception {
		logInfo("Writing all public Ask Pastor series to 'core.html' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PUBLIC));
		env.addFilter(new TypeFilter().withType("Ask Pastor"));

		// get all completed and in-progress series
		Series series = catalog.getFilteredMessagesInASeries();
		series.setTitle("Ask The Pastor");
		series.setDescription(getAskPastorDescription());

		// build the HTML page
		SeriesPageRender pageRender = new SeriesPageRender(series);
		pageRender.setTitle("Ask The Pastor");
		pageRender.setDepartment("Ask The Pastor");
		File outputFile = new File(outputFileDir, "askpastor.html");
		pageRender.render(outputFile);
	}

	private void addCurrentSeriesPromo(Catalog catalog, SeriesIndexWithPromoPageRender render) {
		render.setPromoName("current-series");

		// current promo series is Holy Spirit, so build a series list for it
		List<Series> promoSeries = new ArrayList<>();
		for (Series series : catalog.getSeries())
			if (series.getTitle().startsWith("Holy Spirit (Part ")) {
				promoSeries.add(series);
			}
		Collections.sort(promoSeries, Series.byTitle);
		render.setPromoSeries(promoSeries);
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
		b.append(
				"    <td><img src='https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/remix.jpeg' width='128'/></td>");
		b.append("    <td>");
		b.append("      <h3>");
		b.append(
				"        We&apos;re currently re-editing and bringing the past 10 years of messages and study materials ");
		b.append("        up to date. New content will be added throughout 2015, so check back frequently!");
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
		b.append(
				"    <td valign=\"top\"><img src='https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/Covenant+Partner+Thumb.jpg' width='164'/></td>");
		b.append("    <td>");
		b.append(
				"      <p style=\"color:maroon;font-weight:bold;\">Please do not share access to this page with anyone.</p>");
		b.append(
				"      <p>Any questions about access to this page should be directed to Pastor Vern or Kevin Murray.</p>");
		b.append("      <p>");
		b.append("        Many of these messages may be rough, unedited, or have other quality problems, ");
		b.append(
				"        and we are not prepared to release them to the public <em>yet</em>. However, there may also be ");
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
		b.append(
				"    <td valign=\"top\"><img src='https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/corestaff.jpg' width='164'/></td>");
		b.append("    <td>");
		b.append("      <p>C.O.R.E.: Center of Our Relationship Experiences</p>");
		b.append("      <p>");
		b.append(
				"        Mary Peltz is a certified counselor with A.A.C.C. and is a Co-Pastor at Word of Life Ministries ");
		b.append("        which is affiliated and licensed through ");
		b.append("        <a href=\"http://www.afcminternational.org\" target=\"_blank\">A.F.C.M. International</a>.");
		b.append("      </p>");
		b.append("      <p>");
		b.append("        Mary specializes in communication skills and restoring relationships and families. ");
		b.append(
				"        She administrates C.O.R.E. programs which is a \"Freedom From\" program that brings help to ");
		b.append(
				"        schools, group homes and staff situations. She is currently facilitating C.O.R.E. Programs at ");
		b.append("        the jails in the Northern Minnesota areas.");
		b.append("      </p>");
		b.append("    </td>");
		b.append("  </tr>");
		b.append("</table>");

		return b.toString();
	}

	private String getAskPastorDescription() {
		return "<br/><em>Always be prepared to give an answer to everyone who asks you to give the "
				+ "reason for the hope that you have. (1 Peter 3:15)</em><br/><br/>"
				+ "Too many times we see things in the world around us or find things in the Word of God that we "
				+ "don't understand. If you have questions about what you see, read, or hear, these short messages "
				+ "might have the answers you are looking for. <br/><br/>"
				+ "Pastor Vern fields questions submitted to him from the congregation or anyone online. "
				+ "If you have a question for Pastor Vern, please "
				+ "<a href=\"mailto:wordoflife.mn@gmail.com\">email it to us</a>.<br/><br/>";
	}
}
