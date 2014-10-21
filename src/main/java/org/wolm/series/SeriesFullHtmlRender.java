package org.wolm.series;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.lang3.StringUtils;
import org.wolm.catalog.HtmlRender;

/**
 * Gets a paragraph-block of this series, including everything interesting known about the series.
 */
public class SeriesFullHtmlRender extends HtmlRender {
	private final Series series;

	@Override
	public String getSkinName() {
		return "basic-table";
	}

	public SeriesFullHtmlRender(Series series) {
		super();
		this.series = series;
	}

	@Override
	public String render() throws Exception {

		StringBuilder page = new StringBuilder();
		page.append("<table>");

		// description
		page.append("<tr>\n");
		page.append(getDescriptionRow());
		page.append("<tr>\n");

		// speaker
		page.append("<tr>\n");
		page.append("  <td><b>").append(getSpeakerLabel()).append(":</b></td>\n");
		page.append("  <td>").append(getSpeakers()).append("</td>\n");
		page.append("<tr>\n");

		// dates
		page.append("<tr>\n");
		page.append("  <td><b>").append(getDateLabel()).append(":</b></td>\n");
		page.append("  <td>").append(getDates()).append("</td>\n");
		page.append("<tr>\n");

		page.append("</table>");

		// TODO: kmurray
		// page.append("<table>");
		// for (Message m : series.getMessages()) {
		// page.append("<tr><td>").append(m.getTitle()).append("</td></tr>");
		// }
		// page.append("/table>");

		return page.toString();
	}

	private String getSpeakerLabel() {
		return series.getSpeakers().size() == 1 ? "Speaker" : "Speakers";
	}

	private String getSpeakers() {
		return StringUtils.join(series.getSpeakers(), ", ");
	}

	private String getDateLabel() {
		if (series.getStartDate() == null) return "Date";
		if (series.getEndDate() == null || series.getEndDate().equals(series.getStartDate())) {
			return "Date";
		}
		return "Dates";
	}

	@SuppressWarnings("unused")
	private String getMessageCountAndDates() {
		StringBuilder b = new StringBuilder();

		// number of messages
		b.append(series.getMessageCount());
		b.append(series.getMessageCount() == 1 ? " message" : " messages");

		if (series.getStartDate() == null) return b.toString();

		DateFormat fmt = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
		if (series.getEndDate() == null || series.getEndDate().equals(series.getStartDate())) {
			b.append(" on ").append(fmt.format(series.getStartDate()));
			return b.toString();
		}

		b.append(" from ").append(fmt.format(series.getStartDate()));
		b.append(" to ").append(fmt.format(series.getEndDate()));
		return b.toString();
	}

	private String getDates() {
		// number of messages
		String messageCount = series.getMessageCount() + (series.getMessageCount() == 1 ? " message" : " messages");

		if (series.getStartDate() == null) return messageCount;

		DateFormat fmt = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
		if (series.getEndDate() == null || series.getEndDate().equals(series.getStartDate())) {
			return fmt.format(series.getStartDate()) + " (" + messageCount + ")";
		}

		return fmt.format(series.getStartDate()) + " - " + fmt.format(series.getEndDate()) + " (" + messageCount + ")";
	}

	private String getDescriptionRow() {
		StringBuilder b = new StringBuilder();

		if (series.getCoverArtLink() != null) {
			b.append("  <td valign='top'>");
			b.append(getCoverArt());
			b.append("</td>\n");
		}
		b.append("  <td valign='top'");
		if (series.getCoverArtLink() == null) b.append(" colspan='2'");
		b.append(">");
		b.append("<b>").append(series.getTitle()).append("</b><br/>");
		if (series.getDescription() != null) b.append(series.getDescription());
		b.append("</td>\n");

		return b.toString();

	}

	private String getCoverArt() {
		if (series.getCoverArtLink() == null) return "&nbsp;";

		return "<img src='" + series.getCoverArtLink() + "' width='128'/>";
	}
}
