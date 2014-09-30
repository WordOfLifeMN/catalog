package org.wolm.series;

import org.wolm.catalog.HtmlRender;

/**
 * Gets a paragraph-block of this series, including everything interesting known about the series.
 */
public class SeriesTableHtmlRender extends HtmlRender {
	private final Series series;

	public SeriesTableHtmlRender(Series series) {
		super();
		this.series = series;
	}

	@Override
	public String render() throws Exception {
		// DateFormat fmt = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);

		StringBuilder page = new StringBuilder();
		page.append("<table>");

		// description
		page.append("<tr>\n");
		page.append("  <td><b>Description:</b></td>\n");
		page.append("  <td>").append(series.getDescription()).append("</td>\n");
		page.append("<tr>\n");

		page.append("</table>");
		return page.toString();
	}

}
