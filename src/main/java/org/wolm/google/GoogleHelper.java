package org.wolm.google;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.gdata.client.spreadsheet.SpreadsheetQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

/**
 * Helps access data on the Google Drive.
 * <p>
 * To configure this by default, create a file ~/.wolm/google.properties that contains:
 * <ul>
 * <li>serviceAccountId=
 * </ul>
 * The Service Account ID is the "Email address" of the service account from the
 * <a href="https://console.developers.google.com/project">Developers Console</a>. The spreadsheets must be shared with
 * this account
 * <p>
 * The directory ~/.wolm must also contain the P12 key file for the service account
 * <p>
 * If these files exist, this class will be initialized with the values found therein. If these files do not exist, then
 * you must configure this object manually with {@code setServiceAccountId()} and {@code setP12KeyFile()} before using
 * it.
 * 
 * @author wolm
 */
public class GoogleHelper {
	private final String applicationName;
	private final URL feedUrl;
	private final JsonFactory JSON_FACTORY = new JacksonFactory();
	private final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

	private SpreadsheetService service;
	private String serviceAccountId;
	private File p12KeyFile;

	public GoogleHelper(String applicationName) {
		super();
		this.applicationName = applicationName;

		configure();

		URL feed = null;
		try {
			feed = new URL("https://spreadsheets.google.com/feeds/spreadsheets/private/full");
		}
		catch (MalformedURLException e) {
			// impossible - hard coded URL that is valid
			e.printStackTrace();
		}
		feedUrl = feed;
	}

	public String getServiceAccountId() {
		return serviceAccountId;
	}

	public void setServiceAccountId(String serviceAccountId) {
		this.serviceAccountId = serviceAccountId;
		service = null;
	}

	public File getP12KeyFile() {
		return p12KeyFile;
	}

	public void setP12KeyFile(File p12KeyFile) {
		this.p12KeyFile = p12KeyFile;
		service = null;
	}

	/** @return The spreadsheet service installed with WOLM credentials */
	SpreadsheetService getService() throws AuthenticationException {
		try {
			if (service == null) {
				service = new SpreadsheetService(applicationName);
				service.setOAuth2Credentials(getOAuth2Credentials());
			}
			return service;
		}
		catch (GeneralSecurityException | IOException e) {
			throw new AuthenticationException("Unable to authenticate with Google due to: " + e.getMessage());
		}
	}

	/** @return List of all spreadsheets that the user has access to */
	@Nonnull
	public List<GoogleSpreadsheet> getAllSpreadsheets() throws AuthenticationException, IOException, ServiceException {
		isConfigured();

		try {
			SpreadsheetFeed feed = getService().getFeed(feedUrl, SpreadsheetFeed.class);
			List<SpreadsheetEntry> entries = feed.getEntries();
			List<GoogleSpreadsheet> spreadsheets = new ArrayList<>(entries.size());
			for (SpreadsheetEntry entry : entries) {
				spreadsheets.add(new GoogleSpreadsheet(this, entry));
			}
			return spreadsheets;
		}
		catch (MalformedURLException e) {
			// impossible - hard coded URL that we know is valid
			e.printStackTrace();
			return Collections.emptyList();
		}
	}

	/**
	 * 
	 * @param name Exact name of a spreadsheet
	 * @return The spreadsheet with the exact name specified, or <code>null</code> if not found
	 * @throws IOException
	 * @throws ServiceException
	 */
	@Nullable
	public GoogleSpreadsheet getSpreadsheet(@Nonnull String name) throws IOException, ServiceException {
		isConfigured();

		SpreadsheetQuery query = new SpreadsheetQuery(feedUrl);
		query.setTitleQuery(name);
		query.setTitleExact(true);

		SpreadsheetFeed feed = getService().getFeed(query, SpreadsheetFeed.class);
		List<SpreadsheetEntry> spreadsheets = feed.getEntries();
		return spreadsheets.isEmpty() ? null : new GoogleSpreadsheet(this, spreadsheets.get(0));
	}

	/**
	 * Configures this class from files in the ~/.wolm directory
	 * 
	 */
	private void configure() {
		// read properties
		File propertiesFile = new File(System.getenv("HOME") + "/.wolm/google.properties");
		if (propertiesFile.exists()) {
			try (Reader reader = new FileReader(propertiesFile)) {
				Properties properties = new Properties();
				properties.load(reader);
				setServiceAccountId(properties.getProperty("serviceAccountId"));
			}
			catch (IOException e) {
				// just ignore, the class must be manually configured
			}
		}

		// find the p12 key - assume there is only one
		File configurationDir = new File(System.getenv("HOME") + "/.wolm");
		File[] p12KeyFiles = configurationDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".p12");
			}
		});

		if (p12KeyFiles.length > 0) {
			if (p12KeyFiles.length > 1) {
				System.out
						.println("WARNING: Multiple P12 Key Files were found, using the first one: " + p12KeyFiles[0]);
			}
			setP12KeyFile(p12KeyFiles[0]);
		}
	}

	/**
	 * Checks configuration
	 * 
	 * @return <code>true</code> if the class is configured correctly
	 * @throws IllegalStateException if it is not configured correctly
	 */
	private boolean isConfigured() {
		if (getServiceAccountId() == null)
			throw new IllegalStateException("GoogleHelper has no service account ID configured");
		if (getP12KeyFile() == null) throw new IllegalStateException("GoogleHelper could not find a P12 Key File");
		return true;
	}

	/**
	 * Creates a set of OAuth2 credentials needed to connect to Google API
	 * 
	 * @return
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	private Credential getOAuth2Credentials() throws GeneralSecurityException, IOException {
		GoogleCredential.Builder builder = new GoogleCredential.Builder();
		builder.setTransport(HTTP_TRANSPORT);
		builder.setJsonFactory(JSON_FACTORY);
		builder.setServiceAccountId(getServiceAccountId());
		builder.setServiceAccountScopes(Arrays.asList("https://spreadsheets.google.com/feeds"));
		builder.setServiceAccountPrivateKeyFromP12File(getP12KeyFile());
		return builder.build();
	}
}
