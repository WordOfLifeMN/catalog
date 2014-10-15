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
		page.append("  <td><b>Description:</b></td>\n");
		page.append("  <td>").append(series.getDescription()).append("</td>\n");
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

	private String getDates() {
		if (series.getStartDate() == null) return null;

		DateFormat fmt = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
		if (series.getEndDate() == null || series.getEndDate().equals(series.getStartDate())) {
			return fmt.format(series.getStartDate());
		}
		return fmt.format(series.getStartDate()) + " - " + fmt.format(series.getEndDate());
	}

}
