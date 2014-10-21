package org.wolm.catalog.catalog;

import java.io.File;

import org.wolm.catalog.PageRender;
import org.wolm.catalog.RenderFactory;
import org.wolm.series.Series;
import org.wolm.series.SeriesUrlRender;

public class CatalogSeriesIndexPageRender extends PageRender {
	private final Catalog catalog;

	public CatalogSeriesIndexPageRender(Catalog catalog) {
		super("catalog-index");
		this.catalog = catalog;
	}

	@Override
	public String getSkinName() {
		return "basic";
	}

	@Override
	public void render(File pageFile) throws Exception {
		System.out.println("Writing catalog index to file " + pageFile.getName() + "…");

		addDataToModel("catalog", catalog);

		super.render(pageFile);

		// write out supporting files (i.e. all the series pages)
		File pageDirectory = pageFile.getParentFile();
		for (Series series : catalog.getSeries()) {
			if (series.getVisibility() != Series.Visibility.PUBLIC) {
				System.out.println("Skpping non-Public series " + series.getTitle());
				continue;
			}
			PageRender seriesRender = RenderFactory.getPageRender(getSkinName(), series);
			File seriesFile = new File(pageDirectory, new SeriesUrlRender(series).getFileName());
			seriesRender.render(seriesFile);
		}

	}

	private String getSeriesIndexHtml() throws Exception {
		StringBuilder b = new StringBuilder();
		if (catalog.getSeries() == null) return b.toString();

		b.append("<p>Series</p>");
		b.append("<ul>\n");
		for (Series s : catalog.getSeries()) {
			if (s.getVisibility() != Series.Visibility.PUBLIC) continue;
			b.append("<li>");
			b.append(RenderFactory.getHtmlRender("basic-summary", s).render());
			b.append("</li>\n");
		}
		b.append("</ul>\n");

		return b.toString();
	}

}
