package org.wolm.series;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.wolm.catalog.HtmlRender;

public class SeriesSummaryHtmlRender extends HtmlRender {
	private final Series series;

	public SeriesSummaryHtmlRender(Series series) {
		super();
		this.series = series;
	}

	@Override
	public String render() throws Exception {
		DateFormat fmt = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
		StringBuilder b = new StringBuilder();

		// b.append("<b>").append(series.getTitle()).append("</b>");
		b.append("<a href=\"").append(baseUrl).append(new SeriesUrlRender(series).render()).append("\">")
				.append(series.getTitle()).append("</a>");
		b.append(" - ").append(series.getMessageCount()).append(" messages");
		b.append(" (").append(fmt.format(series.getStartDate())).append("-")
				.append((series.getEndDate() == null ? "" : fmt.format(series.getEndDate()))).append(")");
		return b.toString();
	}
}
