package org.wolm.catalog.catalog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.math.NumberUtils;
import org.wolm.catalog.App;
import org.wolm.catalog.NamedLink;
import org.wolm.catalog.environment.RenderEnvironment;
import org.wolm.google.GoogleHelper;
import org.wolm.google.GoogleRow;
import org.wolm.google.GoogleSpreadsheet;
import org.wolm.google.GoogleWorksheet;
import org.wolm.message.Message;
import org.wolm.series.Series;

/**
 * Definitions:
 * <table>
 * <tr>
 * <td>Booklet</td>
 * <td>A link attached to a Series which is a PDF file.</td>
 * </tr>
 * <tr>
 * <td>Handout</td>
 * <td>A link attached to a Message which is a PDF file.</td>
 * </tr>
 * <tr>
 * <td>Resource</td>
 * <td>A link attached to a Message or a Series which is not a PDF file.</td>
 * </tr>
 * </table>
 * 
 * @author wolm
 */

public class Catalog {
	private RenderEnvironment env = RenderEnvironment.instance();

	private String messageSpreadsheetName = "Messages";
	private List<Message> messages = new ArrayList<>();

	private String seriesSpreadsheetName = "Series";
	private List<Series> series = new ArrayList<>();

	public Catalog() {
		super();
	}

	public String getMessageSpreadsheetName() {
		return messageSpreadsheetName;
	}

	public void setMessageSpreadsheetName(String messageSpreadsheetName) {
		this.messageSpreadsheetName = messageSpreadsheetName;
	}

	/**
	 * @return All messages regardless of visibility
	 */
	public List<Message> getRawMessages() {
		return messages;
	}

	/**
	 * @return All messages that are visible under the current filters
	 */
	public List<Message> getMessages() {
		List<Message> visibleMessages = new ArrayList<>(messages.size());
		for (Message message : messages)
			if (env.shouldInclude(message)) visibleMessages.add(message);
		return visibleMessages;
	}

	public void add(Message message) {
		messages.add(message);
	}

	/**
	 * @return All series regardless of current visibility rules
	 */
	public List<Series> getSeries() {
		return series;
	}

	/**
	 * @return All series that are visible under the current visibility rules
	 */
	public List<Series> getFilteredSeries() {
		List<Series> visibleSeries = new ArrayList<>(series.size());
		for (Series s : series)
			if (env.shouldInclude(s)) visibleSeries.add(s);
		return visibleSeries;
	}

	public void add(Series series) {
		this.series.add(series);
	}

	/**
	 * Reads the entire catalog from the Google message archive. validates and organizes the contents
	 * 
	 * @throws Exception if anything goes wrong
	 */
	public void populateFromGoogleSpreadsheets() throws Exception {
		GoogleHelper google = new GoogleHelper("org-wolm-catalog");

		initMessages(google);
		initSeries(google);

	}

	/**
	 * Reads the message logs and instantiates a list of all messages
	 * 
	 * @param google Google Helper to use
	 * @return A list of all messages read from the message logs
	 */
	private void initMessages(GoogleHelper google) throws Exception {
		GoogleSpreadsheet spreadsheet = google.getSpreadsheet(messageSpreadsheetName);
		if (spreadsheet == null) throw new Exception("Cannot find spreadsheet called '" + messageSpreadsheetName
				+ "' on Google.");

		GoogleWorksheet worksheet = spreadsheet.getWorksheet("Media Log");
		if (worksheet == null) throw new Exception("Cannot find worksheet called 'Media Log' in the '"
				+ messageSpreadsheetName + "' spreadsheet.");

		List<String> columns = worksheet.getColumnNames();
		for (String columnName : new String[] { "date", "name", "speaker", "audiolink", "videolink", "type",
				"visibility", "seriesname", "track", "description", "resources" })
			if (!columns.contains(columnName)) {
				throw new Exception("Cannot find column '" + columnName + "' in the spreadsheet '"
						+ messageSpreadsheetName + "'");
			}

		// create message objects
		for (GoogleRow row : worksheet.getRows()) {
			Message msg = new Message();

			// title/name
			msg.setTitle(row.getValue("name"));

			// date
			msg.setDate(row.getDateValue("date"));

			// series
			String value = row.getValue("seriesname");
			if (value != null) {
				String[] seriesArray = value.split(";");
				List<String> series = new ArrayList<>(seriesArray.length);
				for (String s : seriesArray)
					series.add(s.trim());
				msg.setSeries(series);
			}
			value = row.getValue("track");
			if (value != null) {
				String[] trackArray = value.split(";");
				List<Integer> tracks = new ArrayList<>(trackArray.length);
				for (String s : trackArray)
					tracks.add(NumberUtils.toInt(s.trim()));
				msg.setTrackNumbers(tracks);
			}

			// description
			msg.setDescription(row.getValue("description"));

			// resources
			msg.setResourcesAsString(row.getValue("resources"));

			// type
			msg.setType(row.getValue("type"));

			// visibility
			msg.setVisibilityAsString(row.getValue("visibility"));

			// speakers
			value = row.getValue("speaker");
			if (value != null) {
				String[] speakerArray = value.split(";");
				List<String> speakers = new ArrayList<>(speakerArray.length);
				for (String s : speakerArray)
					speakers.add(s.trim());
				msg.setSpeakers(speakers);
			}

			// audio & video links
			msg.setAudioLinkAsString(row.getValue("audiolink"));
			msg.setVideoLinkAsString(row.getValue("videolink"));

			/*
			 * validate the message is ok to process
			 */
			if (!msg.isValid(System.out)) {
				App.logWarn("Ignoring message due to preceeding problems");
				continue;
			}
			msg.normalize();

			add(msg);
		}
	}

	/**
	 * Reads the series logs and instantiates a list of all series. Does not auto-wire the series to the messages
	 * 
	 * @param google Google Helper to use
	 * @return A list of all series read from the series log
	 */
	private void initSeries(GoogleHelper google) throws Exception {
		// must do messages first so we can record which messages go with each series
		assert !getRawMessages().isEmpty();

		GoogleSpreadsheet spreadsheet = google.getSpreadsheet(seriesSpreadsheetName);
		if (spreadsheet == null) throw new Exception("Cannot find spreadsheet called '" + seriesSpreadsheetName
				+ "' on Google.");

		GoogleWorksheet worksheet = spreadsheet.getWorksheet("Series Log");
		if (worksheet == null) throw new Exception("Cannot find worksheet called 'Series Log' in the '"
				+ seriesSpreadsheetName + "' spreadsheet.");

		List<String> columns = worksheet.getColumnNames();
		for (String columnName : new String[] { "name", "datestarted", "dateended", "messages", "speaker",
				"description", "booklets", "resources", "visibility", "coverart", "coverimage", "webid" })
			if (!columns.contains(columnName)) {
				throw new Exception("Cannot find column '" + columnName + "' in the spreadsheet '"
						+ seriesSpreadsheetName + "'");
			}

		// prepare unique ID validation
		Set<String> seriesIds = new HashSet<>();

		// create series objects
		for (GoogleRow row : worksheet.getRows()) {
			Series series = new Series();

			// title/name
			series.setTitle(row.getValue("name"));

			try {
				App.logDebug("Initializing series '" + series.getTitle() + "'...");
				App.logIndent();

				// unique id
				series.setId(row.getValue("webid"));

				// date
				series.setStartDate(row.getDateValue("datestarted"));
				series.setEndDate(row.getDateValue("dateended"));

				// messages
				series.setMessageCount(row.getLongValue("messages"));

				// speakers
				String value = row.getValue("speaker");
				if (value != null) {
					String[] speakerArray = value.split(";");
					List<String> speakers = new ArrayList<>(speakerArray.length);
					for (String s : speakerArray)
						speakers.add(s.trim());
					series.setSpeakers(speakers);
				}

				// description
				series.setDescription(row.getValue("description"));

				// booklets
				series.setBookletsAsString(row.getValue("booklets"));

				// resources
				series.setResourcesAsString(row.getValue("resources"));

				// visibility
				series.setVisibilityAsString(row.getValue("visibility"));

				// cover
				series.setCoverArtLinkAsString(row.getValue("coverart"));
				series.setCoverImageLinkAsString(row.getValue("coverimage"));

				// discover messages for this series
				series.discoverMessages(messages);

				/*
				 * validate the series is ok to process
				 */
				if (!series.isValid(System.out, seriesIds)) {
					App.logWarn("Ignoring series due to preceeding problems");
					continue;
				}

				series.normalize();

				add(series);
			}
			finally {
				App.logOutdent();
			}
		}
	}

	/**
	 * Gets a list of all non-series messages and returns them wrapped in series. Currently, loose messages are gathered
	 * by year and one series is returned per year.
	 * 
	 * @return List of series representing all loose, stand-alone messages oldest first
	 */
	public List<Series> getStandAloneMessagesInSeries() {
		List<Series> allSeries = new ArrayList<>();
		for (int year = 2000; year < 2030; year++) {
			Series series = getStandAloneMessages(year);
			if (series.getMessageCount() > 0) allSeries.add(series);
		}
		return allSeries;
	}

	/**
	 * Gets all the messages from a given year as a series, but only if they are not in a series already
	 * 
	 * @param year
	 * @return
	 */
	public Series getStandAloneMessages(int year) {
		// sort the messages by date
		List<Message> orderedMessages = new ArrayList<>(messages);
		Collections.sort(orderedMessages, Message.byDate);

		// create series
		Series series = new Series();
		series.setTitle("Messages from " + year + " that are not part of a series");
		series.setId("WOLS-SA" + year);
		series.setDescription("Messages from " + year + " that were not part of any series.");
		series.setVisibility(env.getVisibility());

		// add all the messages newer than the cutoff
		GregorianCalendar cal = new GregorianCalendar();
		for (Message message : orderedMessages) {
			if (message.getDate() == null) break; // nulls are sorted to end

			// bail if not in the right year
			cal.setTime(message.getDate());
			if (cal.get(Calendar.YEAR) != year) continue;
			if (!env.shouldInclude(message)) continue;
			if (!message.getSeries().isEmpty()) continue;
			series.addMessage(message);
		}

		// fill out the dates
		if (series.getMessageCount() > 0) {
			series.setStartDate(series.getMessages().get(0).getDate());
			series.setEndDate(series.getMessages().get(series.getMessages().size() - 1).getDate());
		}

		return series;
	}

	/**
	 * Gets a list of all series that are completed (have a non-<code>null</code> end date)
	 * <p>
	 * Honors the global minimum visibility
	 * 
	 * @return List of all completed series, sorted in date order (oldest first)
	 */
	public List<Series> getCompletedSeries() {
		List<Series> serieses = new ArrayList<>();

		for (Series series : getFilteredSeries()) {
			if (series.getEndDate() == null) continue;
			if (!env.shouldInclude(series)) continue;
			if (series.isBooklet()) continue;
			serieses.add(series);
		}

		return serieses;
	}

	/**
	 * Gets a list of all series that are in progress (have a non-<code>null</code> start date but a {@code null} end
	 * date)
	 * <p>
	 * Honors the global minimum visibility
	 * 
	 * @return List of all in progress series, sorted in date order (oldest first)
	 */
	public List<Series> getInProgressSeries() {
		List<Series> serieses = new ArrayList<>();

		for (Series series : getFilteredSeries()) {
			if (series.getEndDate() != null) continue;
			if (!env.shouldInclude(series)) continue;
			if (series.isBooklet()) continue;
			serieses.add(series);
		}

		return serieses;
	}

	/**
	 * Gets a list of recent series. A recent series is any series with a start date but no end date (assumed to be in
	 * progress still), or any series with a recent end date.
	 * <p>
	 * Honors the global minimum visibility
	 * 
	 * @param days Number of days that should be considered "recent". Only series ended in the past <code>days</code>
	 * days will be returned.
	 * @return List of recent series, sorted by most recent first. Empty list if none
	 */
	public List<Series> getRecentSeries(int days) {
		List<Series> serieses = new ArrayList<>();

		// find cutoff
		GregorianCalendar cal = new GregorianCalendar();
		cal.add(Calendar.DATE, -1 * days);
		Date cutoff = cal.getTime();

		for (Series series : getFilteredSeries()) {
			if (series.getEndDate() != null && series.getEndDate().before(cutoff)) continue;
			if (!env.shouldInclude(series)) continue;
			if (series.isBooklet()) continue;
			serieses.add(series);
		}

		return serieses;
	}

	/**
	 * Creates an artificial series for all recent messages.
	 * <p>
	 * Honors the global minimum visibility
	 * 
	 * @param days How many days old something has to be to no longer be considered "Recent"
	 * @return List of recent messages sorted newest to oldest
	 */
	public Series getRecentMessages(int days) {
		// sort the messages by date
		List<Message> orderedMessages = new ArrayList<>(messages);
		Collections.sort(orderedMessages, Message.byDateDescending);

		// create series
		final Series series = new Series();
		series.setTitle("Recent Messages from Word of Life Ministries");
		series.setDescription("Recent messages from the last " + days + " days.");
		series.setVisibility(env.getVisibility());

		// find cutoff
		GregorianCalendar cal = new GregorianCalendar();
		cal.add(Calendar.DATE, -1 * days);
		final Date cutoff = cal.getTime();

		// add all the messages newer than the cutoff
		for (Message message : orderedMessages) {
			if (message.getDate() == null) break; // nulls are sorted to end
			if (message.getDate().before(cutoff)) break;
			if (!env.shouldInclude(message)) continue;
			series.addMessage(message);
		}

		return series;
	}

	/**
	 * Finds all handouts (downloadable documents) or resources (links like YouTube or website) from any series, or
	 * message. Does not include Booklets
	 * 
	 * @return List of all resources
	 */
	public List<NamedLink> getHandoutsAndResources() {
		List<NamedLink> resources = new ArrayList<>();

		// find all resources

		// find all resources from series, which will include messages in those series
		for (Series series : getFilteredSeries()) {
			if (!env.shouldInclude(series)) continue;
			for (NamedLink resource : series.getResources(true)) {
				// if (!resource.isDocumentForDownload()) continue;
				resources.add(resource);
			}
		}

		// find resources from stand-alone messages that are not part of any series
		for (Message message : getMessages()) {
			if (!message.getSeries().isEmpty()) continue;
			if (!env.shouldInclude(message)) continue;
			for (NamedLink resource : message.getResources()) {
				// if (!resource.isDocumentForDownload()) continue;
				resources.add(resource);
			}
		}

		// remove those that are booklets
		resources.removeAll(getBooklets());

		Collections.sort(resources, NamedLink.byTitleName);
		return resources;
	}

	/**
	 * Finds all downloadable booklets. Booklets are from the Series Booklets column. Booklets are always public,
	 * regardless of visibility of the series. However, if the series isn't visible, then the booklet link will not
	 * cross-reference back to the series
	 * 
	 * @return List of booklets
	 */
	public List<NamedLink> getBooklets() {
		List<NamedLink> booklets = new ArrayList<>();

		// find booklets of all series, regardless of visibility
		for (Series series : getSeries()) {
			for (NamedLink booklet : series.getBooklets()) {
				if (!booklet.isDocumentForDownload()) continue;
				// if the series is not visible, then break the reference back to it by converting it from a
				// NamedResourceLink to just a NamedLink
				if (!env.shouldInclude(series)) {
					booklet = new NamedLink(booklet);
				}
				booklets.add(booklet);
			}
		}

		Collections.sort(booklets, NamedLink.byTitleName);
		return booklets;
	}

	public void sortSeriesByDate() {
		Collections.sort(series, Series.byDate);
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();

		if (getRawMessages() != null) {
			for (Message m : getRawMessages()) {
				b.append(m).append('\n');
			}
		}

		return b.toString();
	}

}
