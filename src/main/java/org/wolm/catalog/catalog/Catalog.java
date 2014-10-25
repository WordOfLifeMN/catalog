package org.wolm.catalog.catalog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.wolm.catalog.RenderFactory;
import org.wolm.google.GoogleHelper;
import org.wolm.google.GoogleRow;
import org.wolm.google.GoogleSpreadsheet;
import org.wolm.google.GoogleWorksheet;
import org.wolm.message.Message;
import org.wolm.series.Series;

public class Catalog {
	private String messageSpreadsheetName = "Messages";
	private List<Message> messages;

	private String seriesSpreadsheetName = "Series";
	private List<Series> series;

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
		if (messages == null) return null;
		List<Message> visibleMessages = new ArrayList<>(messages.size());

		for (Message message : messages)
			if (RenderFactory.isVisible(message.getVisibility())) visibleMessages.add(message);

		return visibleMessages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
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
		if (series == null) return null;
		List<Series> visibleSeries = new ArrayList<>(series.size());

		for (Series s : series)
			if (RenderFactory.isVisible(s.getVisibility())) visibleSeries.add(s);

		return visibleSeries;
	}

	public void setSeries(List<Series> series) {
		this.series = series;
	}

	/**
	 * Reads the entire catalog from the Google message archive. validates and organizes the contents
	 * 
	 * @throws Exception if anything goes wrong
	 */
	public void init() throws Exception {
		GoogleHelper google = new GoogleHelper("org-wolm-catalog");

		this.messages = initMessages(google);
		this.series = initSeries(google);

	}

	/**
	 * Reads the message logs and instantiates a list of all messages
	 * 
	 * @param google Google Helper to use
	 * @return A list of all messages read from the message logs
	 */
	private List<Message> initMessages(GoogleHelper google) throws Exception {
		GoogleSpreadsheet spreadsheet = google.getSpreadsheet(messageSpreadsheetName);
		if (spreadsheet == null) throw new Exception("Cannot find spreadsheet called '" + messageSpreadsheetName
				+ "' on Google.");

		GoogleWorksheet worksheet = spreadsheet.getWorksheet("Media Log");
		if (worksheet == null) throw new Exception("Cannot find worksheet called 'Media Log' in the '"
				+ messageSpreadsheetName + "' spreadsheet.");

		List<String> columns = worksheet.getColumnNames();
		for (String columnName : new String[] { "date", "name", "speaker", "audiolink", "videolink", "type",
				"visibility", "seriesname", "track", "description" })
			if (!columns.contains(columnName)) {
				throw new Exception("Cannot find column '" + columnName + "' in the spreadsheet '"
						+ messageSpreadsheetName + "'");
			}

		// create message objects
		List<Message> messages = new ArrayList<>(1000);
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
					tracks.add(NumberUtils.toInt(s));
				msg.setTrackNumbers(tracks);
			}

			// description
			msg.setDescription(row.getValue("description"));

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
				System.out.println("WARNING: Ignoring message due to preceeding problems");
				continue;
			}

			messages.add(msg);
		}

		return messages;
	}

	/**
	 * Reads the series logs and instantiates a list of all series. Does not auto-wire the series to the messages
	 * 
	 * @param google Google Helper to use
	 * @return A list of all series read from the series log
	 */
	private List<Series> initSeries(GoogleHelper google) throws Exception {
		// must do messages first so we can record which messages go with each series
		assert messages != null;

		GoogleSpreadsheet spreadsheet = google.getSpreadsheet(seriesSpreadsheetName);
		if (spreadsheet == null) throw new Exception("Cannot find spreadsheet called '" + seriesSpreadsheetName
				+ "' on Google.");

		GoogleWorksheet worksheet = spreadsheet.getWorksheet("Series Log");
		if (worksheet == null) throw new Exception("Cannot find worksheet called 'Series Log' in the '"
				+ seriesSpreadsheetName + "' spreadsheet.");

		List<String> columns = worksheet.getColumnNames();
		for (String columnName : new String[] { "name", "datestarted", "dateended", "messages", "speaker",
				"description", "visibility", "coverart", "coverimage", "studyguide", "webid" })
			if (!columns.contains(columnName)) {
				throw new Exception("Cannot find column '" + columnName + "' in the spreadsheet '"
						+ seriesSpreadsheetName + "'");
			}

		// create series objects
		List<Series> serieses = new ArrayList<>(100);
		for (GoogleRow row : worksheet.getRows()) {
			Series series = new Series();

			// unique id
			series.setId(row.getValue("webid"));

			// title/name
			series.setTitle(row.getValue("name"));

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
			series.setStudyGuideLinksAsString(row.getValue("studyguide"));

			// discover messages for this series
			series.discoverMessages(messages);

			/*
			 * validate the series is ok to process
			 */
			if (!series.isValid(System.out)) {
				System.out.println("    WARNING: Ignoring series due to preceeding problems");
				continue;
			}

			serieses.add(series);
		}

		return serieses;
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
		Series series = new Series();
		series.setTitle("Recent Messages from Word of Life Ministries");
		series.setDescription("Recent messages from the last " + days + " days.");

		// find cutoff
		GregorianCalendar cal = new GregorianCalendar();
		cal.add(Calendar.DATE, -1 * days);
		Date cutoff = cal.getTime();

		// add all the messages newer than the cutoff
		for (Message message : orderedMessages) {
			if (message.getDate() == null) break; // nulls are sorted to end
			if (message.getDate().before(cutoff)) break;
			if (!RenderFactory.isVisible(message.getVisibility())) continue;
			series.addMessage(message);
		}

		return series;
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
