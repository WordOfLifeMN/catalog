package org.wolm.catalog.catalog;

import java.net.URL;
import java.util.Map;

import org.wolm.catalog.PageRender;
import org.wolm.series.Series;
import org.wolm.series.SeriesSummaryHtmlRender;
import org.wolm.weebly.WeeblyPage;

public class CatalogSeriesIndexPageRender extends PageRender {
	private final Catalog catalog;

	public CatalogSeriesIndexPageRender(Catalog catalog) throws Exception {
		this(null, catalog);
	}

	public CatalogSeriesIndexPageRender(URL templateUrl, Catalog catalog) throws Exception {
		super(templateUrl);
		this.catalog = catalog;
	}

	@Override
	public WeeblyPage render() throws Exception {
		// read the Weebly template page
		WeeblyPage page = preparePage();

		// create a map of content to substitute
		Map<String, String> content = prepareContent("Word of Life Ministries Online Series");
		content.put("Content", getSeriesIndexHtml());

		// insert the content
		page.substituteVariables(content);

		return page;
	}

	private String getSeriesIndexHtml() throws Exception {
		StringBuilder b = new StringBuilder();
		if (catalog.getSeries() == null) return b.toString();

		b.append("<p>Series</p>");
		b.append("<ul>\n");
		for (Series s : catalog.getSeries()) {
			b.append("<li>").append(new SeriesSummaryHtmlRender(s).render()).append("</li>\n");
		}
		b.append("</ul>\n");

		return b.toString();
	}

}
