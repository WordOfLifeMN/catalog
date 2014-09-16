package org.wolm.google;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gdata.client.spreadsheet.SpreadsheetQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

/**
 * Helps access data on the Google Drive.
 * <p>
 * To configure this by default, create a file ~/.wolm/googledrive.properties that contains:
 * <ul>
 * <li>username=
 * <li>password=
 * </ul>
 * If this file exists, this class will be initialized with the values found therein. If this file does not exist, then
 * you must configure this object manually with {@code setUserName()} and {@code setPassword()} before using it.
 * 
 * @author wolm
 */
public class GoogleHelper {
	private final String applicationName;
	private final URL feedUrl;

	private SpreadsheetService service;
	private String userName;
	private String password;

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

	public void setUserName(String userName) {
		this.userName = userName;
		service = null;
	}

	public void setPassword(String password) {
		this.password = password;
		service = null;
	}

	/** @return The spreadsheet service installed with WOLM credentials */
	SpreadsheetService getService() throws AuthenticationException {
		if (service == null) {
			service = new SpreadsheetService(applicationName);
			service.setUserCredentials(userName, password);
		}
		return service;
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
	 * Configures this class from a properties file on disk
	 * 
	 */
	private void configure() {
		File propertiesFile = new File(System.getenv("HOME") + "/.wolm/googledrive.properties");
		if (!propertiesFile.exists()) return;

		try (Reader reader = new FileReader(propertiesFile)) {
			Properties properties = new Properties();
			properties.load(reader);
			setUserName(properties.getProperty("username"));
			setPassword(properties.getProperty("password"));
		}
		catch (IOException e) {
			// just ignore, the class must be manually configured
		}
	}

	/**
	 * Checks configuration
	 * 
	 * @return <code>true</code> if the class is configured correctly
	 * @throws IllegalStateException if it is not configured correctly
	 */
	private boolean isConfigured() {
		if (userName == null) throw new IllegalStateException("GoogleHelper has no user name configured");
		if (password == null) throw new IllegalStateException("GoogleHelper has no password configured");
		return true;
	}

}
