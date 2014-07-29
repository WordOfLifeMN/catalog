package org.wolm.google;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.gdata.client.spreadsheet.SpreadsheetQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class GoogleHelper {
	private final String applicationName;
	private final URL feedUrl;

	private SpreadsheetService service;
	private String userName = "wordoflife.mn@gmail.com";
	private String password = "Minn2004";

	public GoogleHelper(String applicationName) {
		super();
		this.applicationName = applicationName;

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

		SpreadsheetQuery query = new SpreadsheetQuery(feedUrl);
		query.setTitleQuery(name);
		query.setTitleExact(true);

		SpreadsheetFeed feed = getService().getFeed(query, SpreadsheetFeed.class);
		List<SpreadsheetEntry> spreadsheets = feed.getEntries();
		return spreadsheets.isEmpty() ? null : new GoogleSpreadsheet(this, spreadsheets.get(0));
	}

}
