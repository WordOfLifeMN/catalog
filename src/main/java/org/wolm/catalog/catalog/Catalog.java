package org.wolm.catalog.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
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

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	public List<Series> getSeries() {
		return series;
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

		this.series = initSeries(google);
		this.messages = initMessages(google);
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

	public void sortSeriesByDate() {
		Collections.sort(series, new Comparator<Series>() {
			public int compare(Series series1, Series series2) {
				Date date1 = series1.getStartDate();
				Date date2 = series2.getStartDate();
				if (date1 == null && date2 == null) return 0;
				if (date1 == null) return 1;
				if (date2 == null) return -1;
				return date1.before(date2) ? -1 : (date1.equals(date2) ? 0 : 1);
			}
		});
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();

		if (getMessages() != null) {
			for (Message m : getMessages()) {
				b.append(m).append('\n');
			}
		}

		return b.toString();
	}

	public String getMessageIndexHtml() {
		StringBuilder b = new StringBuilder();
		if (getMessages() == null) return b.toString();

		b.append("<p>Messages</p>");
		b.append("<ul>\n");
		for (Message m : getMessages()) {
			b.append("<li>").append(m.getOneLineHtmlSummary()).append("</li>\n");
		}
		b.append("</ul>\n");

		return b.toString();
	}
}
