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
import org.wolm.catalog.catalog.MediaCatalog;
import org.wolm.catalog.catalog.ResourcesPageRender;
import org.wolm.catalog.catalog.SeriesIndexPageRender;
import org.wolm.catalog.catalog.SeriesIndexWithPromoPageRender;
import org.wolm.catalog.environment.BookletFilter;
import org.wolm.catalog.environment.EntirelyWithinYearFilter;
import org.wolm.catalog.environment.IntersectingWithYearFilter;
import org.wolm.catalog.environment.MinistryFilter;
import org.wolm.catalog.environment.RecentFilter;
import org.wolm.catalog.environment.RenderEnvironment;
import org.wolm.catalog.environment.StartedWithinYearFilter;
import org.wolm.catalog.environment.VisibilityFilter;
import org.wolm.message.Message;
import org.wolm.prophesy.PropheciesPageRender;
import org.wolm.prophesy.Prophecy;
import org.wolm.prophesy.ProphecyCatalog;
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

	@Parameter(names = { "--for-upload" }, description = "Prepare files for S3 (use S3 URLs).")
	private boolean prepareForUpload = false;

	@Parameter(names = { "--do-upload" }, description = "Upload final files to S3.")
	private boolean uploadToS3 = false;

	private RenderEnvironment env = RenderEnvironment.instance();

	private String wolS3BucketName = "wordoflife.mn.catalog";
	// private String tboS3BucketName = "thebridgeoutreach.mn.catalog";
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

		app.mediaCatalog();
		app.prophecyCatalog();

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
		if (forUpload()) {
			// uploading to S3: the baseref is the URL to the s3 bucket
			RenderFactory.setBaseRef("http://s3-us-west-2.amazonaws.com/" + computeS3BucketName()
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

	/**
	 * Determines whether the pages should be built for uploading to S3 or not. This affects the URLs used for
	 * referencing files
	 * 
	 * @return <code>true</code> if we should build pages for uploading to S3. <code>false</code> if the pages should be
	 * built for file browsing
	 */
	public boolean forUpload() {
		return prepareForUpload;
	}

	/**
	 * Determines whether we should actually upload the files to S3
	 * 
	 * @return <code>true</code> to upload the files, <code>false</code> to just leave them local
	 */
	public boolean doUpload() {
		return uploadToS3;
	}

	public void setUpload(boolean upload) {
		this.prepareForUpload = upload;
	}

	public String computeS3BucketName() {
		return wolS3BucketName;
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
	public void mediaCatalog() throws Exception {

		logInfo("Downloading catalog from Google ...");
		MediaCatalog catalog = new MediaCatalog("WOL Series", "WOL Messages");
		catalog.populateFromGoogleSpreadsheets();

		// Word of Life
		buildRecentMessages("WOL", catalog);
		buildRecentSeries("WOL", catalog);
		buildSeriesForYear("WOL", catalog, 2017, InclusionPolicy.startedWithin);
		buildSeriesForYear("WOL", catalog, 2017, InclusionPolicy.intersectingWith);
		buildPublicCatalog("WOL", catalog);
		buildHandoutsAndResources("WOL", catalog);
		buildBooklets("WOL", catalog);

		buildCovenantPartnerCatalog("WOL", catalog);

		// The Bridge Outreach
		buildPublicCatalog("TBO", catalog);

		// C.O.R.E.
		buildPublicCatalog("CORE", catalog);

		// Ask the Pastor
		buildPublicCatalog("Ask Pastor", catalog);

		logInfo("Catalog file generation is complete");
	}

	/**
	 * @param name Basic file name for the page
	 * @param ministry Ministry this page is for
	 * @return A version of the file name that is for the specific ministry
	 */
	private String computeFileNameForSite(String name, String ministry) {
		if (ministry.equals("WOL")) return name;

		// make a file-safe version of the ministry
		ministry = ministry.toLowerCase().replaceAll("[^a-z0-9]+", "");

		int dot = name.lastIndexOf('.');
		if (dot == -1) return name + ministry;
		return name.substring(0, dot) + "." + ministry + name.substring(dot);
	}

	private String computeMinistryName(String ministry) {
		switch (ministry) {
		case "WOL":
			return "Word of Life Ministries";
		case "TBO":
			return "The Bridge Outreach";
		case "CORE":
			return "C.O.R.E.";
		case "Ask Pastor":
			return "Ask the Pastor";
		default:
			throw new IllegalStateException("Unknown ministry '" + ministry + "'");
		}
	}

	private void buildRecentMessages(String ministry, MediaCatalog catalog) throws Exception {
		final String fileName = computeFileNameForSite("recent-messages.html", ministry);
		logInfo("Writing recent messages to '" + fileName + "' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PUBLIC));
		env.addFilter(new MinistryFilter().with(ministry));
		env.addFilter(new RecentFilter().withDays(60));
		env.addFilter(new BookletFilter(false));

		// find messages
		Series recentMessages = catalog.getFilteredMessagesInASeries();
		recentMessages.setTitle("Recent Messages from " + computeMinistryName(ministry));
		recentMessages.setDescription("Recent messages from the last " + 60 + " days.");
		recentMessages.sortMessages(Message.byDateDescending);

		PageRender pageRender = new SeriesPageRender(recentMessages);
		pageRender.setMinistry(ministry);
		File outputFile = new File(outputFileDir, fileName);
		pageRender.render(outputFile);
	}

	private void buildRecentSeries(String ministry, MediaCatalog catalog) throws Exception {
		final String fileName = computeFileNameForSite("recent-series.html", ministry);
		logInfo("Writing recent series to '" + fileName + "' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PUBLIC));
		env.addFilter(new MinistryFilter().with(ministry));
		env.addFilter(new RecentFilter().withDays(60));
		env.addFilter(new BookletFilter(false));

		// find series
		List<Series> series = catalog.getFilteredSeries();
		series.addAll(catalog.getStandAloneMessagesInSeriesByMessage());
		Collections.sort(series, Series.byTitle);

		// build the page renderer
		PageRender pageRender = new SeriesIndexWithPromoPageRender(series);
		pageRender.setTitle("Recent Series from " + computeMinistryName(ministry));
		addCurrentSeriesPromo(catalog, (SeriesIndexWithPromoPageRender) pageRender);
		pageRender.setMinistry(ministry);
		File outputFile = new File(outputFileDir, fileName);
		pageRender.render(outputFile);
	}

	/**
	 * Gets series for a specific year.
	 * 
	 * @param ministry Name of ministry to build page for
	 * @param catalog Source catalog
	 * @param year Year to extract series for
	 * @param inclusion Determines how to select whether a series is in a year or not
	 * @throws Exception
	 */
	private void buildSeriesForYear(String ministry, MediaCatalog catalog, int year, InclusionPolicy inclusion)
			throws Exception {
		final String fileName = computeFileNameForSite(inclusion.toString() + year + "-series.html", ministry);
		logInfo("Writing " + year + " series to '" + fileName + "' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PUBLIC));
		env.addFilter(new MinistryFilter().with(ministry));
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
		Collections.sort(series, Series.byTitle);

		PageRender pageRender = new SeriesIndexWithPromoPageRender(series);
		pageRender.setTitle("Messages and Series from " + computeMinistryName(ministry) + " in " + year);
		pageRender.setMinistry(ministry);
		if (year == new GregorianCalendar().get(Calendar.YEAR)) {
			addCurrentSeriesPromo(catalog, (SeriesIndexWithPromoPageRender) pageRender);
		}
		File outputFile = new File(outputFileDir, fileName);
		pageRender.render(outputFile);
	}

	private void buildPublicCatalog(String ministry, MediaCatalog catalog) throws Exception {
		final String fileName = computeFileNameForSite("catalog.html", ministry);
		logInfo("Writing all public series to '" + fileName + "' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PUBLIC));
		env.addFilter(new MinistryFilter().with(ministry));

		// get all completed and in-progress series plus all stand-alone messages
		List<Series> catalogSeries = catalog.getCompletedSeries();
		catalogSeries.addAll(catalog.getInProgressSeries());
		// catalogSeries.addAll(catalog.getStandAloneMessagesInSeriesByYear());
		catalogSeries.addAll(catalog.getStandAloneMessagesInSeriesByMessage());
		catalogSeries = SeriesHelper.withoutDuplicates(catalogSeries);
		Collections.sort(catalogSeries, Series.byTitle);

		PageRender pageRender = new SeriesIndexPageRender(catalogSeries);
		pageRender.setTitle(computeMinistryName(ministry) + " Catalog");
		pageRender.setMinistry(ministry);
		((SeriesIndexPageRender) pageRender).setIndexDescription(getCatalogIndexDescription());
		File outputFile = new File(outputFileDir, fileName);
		pageRender.render(outputFile);
	}

	private void buildHandoutsAndResources(String ministry, MediaCatalog catalog) throws Exception {
		final String fileName = computeFileNameForSite("resources.html", ministry);
		logInfo("Writing handouts and resources to '" + fileName + "' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PUBLIC));
		env.addFilter(new MinistryFilter().with(ministry));

		List<NamedLink> resources = catalog.getHandoutsAndResources();
		PageRender pageRender = new ResourcesPageRender(resources);
		pageRender.setTitle("Handouts and Resources");
		pageRender.setMinistry(ministry);
		File outputFile = new File(outputFileDir, fileName);
		pageRender.render(outputFile);
	}

	private void buildBooklets(String ministry, MediaCatalog catalog) throws Exception {
		final String fileName = computeFileNameForSite("booklets.html", ministry);
		logInfo("Writing booklets to '" + fileName + "' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PUBLIC));
		env.addFilter(new MinistryFilter().with());

		List<NamedLink> resources = catalog.getBooklets();
		PageRender pageRender = new BookletsPageRender(resources);
		pageRender.setTitle("Booklets");
		pageRender.setMinistry(ministry);
		File outputFile = new File(outputFileDir, fileName);
		pageRender.render(outputFile);
	}

	private void buildCovenantPartnerCatalog(String ministry, MediaCatalog catalog) throws Exception {
		final String fileName = computeFileNameForSite("catalog-cpartner.html", ministry);
		logInfo("Writing all protected series to '" + fileName + "' ...");

		// prepare environment
		env.clearFilters();
		env.addFilter(new VisibilityFilter(AccessLevel.PROTECTED));
		env.addFilter(new MinistryFilter().with(ministry));

		// get all completed and in-progress series
		List<Series> catalogSeries = catalog.getCompletedSeries();
		catalogSeries.addAll(catalog.getInProgressSeries());
		// catalogSeries.addAll(catalog.getStandAloneMessagesInSeriesByYear());
		catalogSeries.addAll(catalog.getStandAloneMessagesInSeriesByMessage());
		catalogSeries = SeriesHelper.withoutDuplicates(catalogSeries);
		Collections.sort(catalogSeries, Series.byTitle);

		SeriesIndexPageRender pageRender = new SeriesIndexPageRender(catalogSeries);
		pageRender.setTitle("Covenant Partner Catalog for " + computeMinistryName(ministry));
		pageRender.setIndexDescription(getCovenantPartnerIndexDescription());
		pageRender.setMinistry(ministry);
		File outputFile = new File(outputFileDir, fileName);
		pageRender.render(outputFile);
	}

	private void addCurrentSeriesPromo(MediaCatalog catalog, SeriesIndexWithPromoPageRender render) {
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

	/**
	 * Generates the catalog of Word of Life Ministries prophesies
	 * 
	 * @throws IOException
	 */
	public void prophecyCatalog() throws Exception {

		logInfo("Downloading prophesies from AWS ...");
		logIndent();
		ProphecyCatalog catalog = new ProphecyCatalog("wordoflife.mn.prophecy", null);
		catalog.populateFromAwsDocuments(computeS3BucketName(), outputFileDir);
		logOutdent();

		// Word of Life
		buildProphecies("WOL", catalog);

		logInfo("Prophecy file generation is complete");
	}

	private void buildProphecies(String ministry, ProphecyCatalog catalog) throws Exception {
		final String fileName = computeFileNameForSite("prophecies.html", ministry);
		logInfo("Writing prophecies to '" + fileName + "' ...");
		logIndent();

		List<Prophecy> prophecies = catalog.getProphecies();
		Collections.sort(prophecies, Prophecy.byDateDescending);
		PageRender pageRender = new PropheciesPageRender(prophecies);
		pageRender.setTitle("Prophecies Given at Word of Life");
		pageRender.setMinistry(ministry);
		File outputFile = new File(outputFileDir, fileName);
		pageRender.render(outputFile);

		logOutdent();
	}

	/** Upload all pages that have been created to S3 (if requested) */
	public void upload() throws Exception {
		// bail if we're not supposed to upload
		if (!doUpload()) return;

		AwsS3Helper s3Helper = AwsS3Helper.instance();
		Bucket catalogBucket = s3Helper.getBucket(computeS3BucketName());
		if (catalogBucket == null)
			throw new Exception("Cannot find the catalog bucket: '" + computeS3BucketName() + "'");

		logInfo("Uploading all generated pages to the " + computeS3BucketName() + " S3 bucket ...");
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
		if (instance == null || instance.isVerbose()) System.out.println(logIndent + msg);
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

		// b.append("<table>");
		// b.append(" <tr>");
		// b.append(" <td><img src='https://s3-us-west-2.amazonaws.com/" + computeS3BucketName()
		// + "/remix.jpeg' width='128'/></td>");
		// b.append(" <td>");
		// b.append(" <h3>");
		// b.append(
		// " We&apos;re currently re-editing and bringing the past 10 years of messages and study materials ");
		// b.append(" up to date. New content will be added throughout 2017, so check back frequently!");
		// b.append(" </h3>");
		// b.append(" </td>");
		// b.append(" </tr>");
		// b.append("</table>");
		//
		return b.toString();
	}

	private String getCovenantPartnerIndexDescription() {
		StringBuilder b = new StringBuilder();

		b.append("<table>");
		b.append("  <tr>");
		b.append("    <td valign=\"top\"><img src='https://s3-us-west-2.amazonaws.com/" + computeS3BucketName()
				+ "/CovenantPartnerThumb.jpg' width='164'/></td>");
		b.append("    <td>");
		b.append(
				"      <p style=\"color:red;font-weight:bold;\">Please do not share access to this page with anyone.</p>");
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

}
