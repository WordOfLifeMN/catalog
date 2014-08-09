package org.wolm.catalog;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.wolm.google.GoogleHelper;
import org.wolm.google.GoogleRow;
import org.wolm.google.GoogleSpreadsheet;
import org.wolm.google.GoogleWorksheet;

public class Catalog {
	private String messageSpreadsheetName = "Messages";
	private List<Message> messages;

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

	/**
	 * Reads the entire catalog from the Google message archive. validates and organizes the contents
	 * 
	 * @throws Exception if anything goes wrong
	 */
	public void init() throws Exception {
		GoogleHelper google = new GoogleHelper("org-wolm-catalog");

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
		if (worksheet == null) throw new Exception("Cannot find worksheet called 'Media Log' in the spreadsheet.");

		List<String> columns = worksheet.getColumnNames();
		for (String columnName : new String[] { "date", "name", "speaker", "audiolink", "videolink", "type",
				"visibility", "seriesname", "track", "description" })
			if (!columns.contains(columnName)) {
				throw new Exception("Cannot find column '" + columnName + "' in the spreadsheet");
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

	public String toHtml() {
		StringBuffer b = new StringBuffer();

		if (getMessages() != null) {
			b.append("<div><ul>\n");
			for (Message m : getMessages()) {
				b.append("<li>").append(m.toHtml()).append("</li>\n");
			}
			b.append("</ul></div>\n");
		}

		return b.toString();
	}
}
