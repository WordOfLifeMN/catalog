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
import org.wolm.catalog.RenderFactory;
import org.wolm.google.GoogleHelper;
import org.wolm.google.GoogleRow;
import org.wolm.google.GoogleSpreadsheet;
import org.wolm.google.GoogleWorksheet;
import org.wolm.message.Message;
import org.wolm.series.Series;

public class Catalog {
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
	 * @return All messages that are visible under the current visibility restrictions
	 */
	public List<Message> getMessages() {
		List<Message> visibleMessages = new ArrayList<>(messages.size());
		for (Message message : messages)
			if (isVisible(message)) visibleMessages.add(message);
		return visibleMessages;
	}

	public void add(Message message) {
		messages.add(message);
	}

	/**
	 * @return All series regardless of current visibility rules
	 */
	public List<Series> getRawSeries() {
		return series;
	}

	/**
	 * @return All series that are visible under the current visibility rules
	 */
	public List<Series> getSeries() {
		List<Series> visibleSeries = new ArrayList<>(series.size());
		for (Series s : series)
			if (RenderFactory.isExactlyVisible(s.getVisibility())) visibleSeries.add(s);
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
				"description", "visibility", "coverart", "coverimage", "resources", "webid" })
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

				// visibility
				series.setVisibilityAsString(row.getValue("visibility"));

				// cover
				series.setCoverArtLinkAsString(row.getValue("coverart"));
				series.setCoverImageLinkAsString(row.getValue("coverimage"));

				// study guide
				series.setResourcesAsString(row.getValue("resources"));

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
	 * Gets a list of all non-series messages by year and concatenates all completed series
	 * 
	 * @return List of series, starting with years with stand-alone messages followed by all the completed series,
	 * oldest first
	 */
	public List<Series> getCompletedSeriesWithStandAloneMessages() {
		List<Series> allSeries = new ArrayList<>();

		// add the stand-alone messages
		for (int year = 2000; year < 2020; year++) {
			Series series = getStandAloneMessages(year);
			if (series.getMessageCount() > 0) allSeries.add(series);
		}

		// add the completed
		allSeries.addAll(getCompletedSeries());
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
		series.setVisibility(RenderFactory.getMinVisibility());

		// add all the messages newer than the cutoff
		GregorianCalendar cal = new GregorianCalendar();
		for (Message message : orderedMessages) {
			if (message.getDate() == null) break; // nulls are sorted to end

			// bail if not in the right year
			cal.setTime(message.getDate());
			if (cal.get(Calendar.YEAR) != year) continue;
			if (!isVisible(message)) continue;
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

		for (Series series : getSeries()) {
			if (series.getEndDate() == null) continue;
			if (!isVisible(series)) continue;
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

		for (Series series : getSeries()) {
			if (series.getEndDate() != null && series.getEndDate().before(cutoff)) continue;
			if (!isVisible(series)) continue;
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

		// find cutoff
		GregorianCalendar cal = new GregorianCalendar();
		cal.add(Calendar.DATE, -1 * days);
		final Date cutoff = cal.getTime();

		// add all the messages newer than the cutoff
		for (Message message : orderedMessages) {
			if (message.getDate() == null) break; // nulls are sorted to end
			if (message.getDate().before(cutoff)) break;
			if (!isVisible(message)) continue;
			series.addMessage(message);
		}

		return series;
	}

	/**
	 * Finds all downloadable (document) resources from any series, or message. Does not include standalone booklet
	 * resources
	 * 
	 * @return List of all resources
	 */
	public List<NamedLink> getResources() {
		List<NamedLink> resources = new ArrayList<>();

		// find all resources from series, which will include messages in those series
		for (Series series : getSeries()) {
			if (!isVisible(series)) continue;
			if (series.isBooklet()) continue;
			for (NamedLink resource : series.getResources(true)) {
				if (!resource.isDocumentForDownload()) continue;
				resources.add(resource);
			}
		}

		// find resources from stand-alone messages that are not part of any series
		for (Message message : getMessages()) {
			if (!message.getSeries().isEmpty()) continue;
			if (!isVisible(message)) continue;
			for (NamedLink resource : message.getResources()) {
				if (!resource.isDocumentForDownload()) continue;
				resources.add(resource);
			}
		}

		Collections.sort(resources, NamedLink.byName);
		return resources;
	}

	/**
	 * Finds all downloadable (document) booklets. These are stored as series that have {@code Series.isBooklet()}
	 * returns {@code true}
	 * 
	 * @return List of booklets
	 */
	public List<NamedLink> getBooklets() {
		List<NamedLink> resources = new ArrayList<>();

		// find resources from stand-alone messages that are not part of any series
		for (Series series : getSeries()) {
			if (!isVisible(series)) continue;
			if (!series.isBooklet()) continue;
			for (NamedLink resource : series.getResources(true)) {
				if (!resource.isDocumentForDownload()) continue;
				resources.add(resource);
			}
		}

		Collections.sort(resources, NamedLink.byName);
		return resources;
	}

	/**
	 * @param series A series
	 * @return {@code true} if the series is visible according to the current visibility level defined in the
	 * RenderFactory
	 */
	private boolean isVisible(Series series) {
		return RenderFactory.isAtLeastVisible(series.getVisibility());
	}

	/**
	 * @param message A message
	 * @return {@code true} if the message is visible according to the current visibility level defined in the
	 * RenderFactory
	 */
	private boolean isVisible(Message message) {
		return RenderFactory.isAtLeastVisible(message.getVisibility());
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
