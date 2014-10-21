package org.wolm.catalog.catalog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Map;

import org.wolm.catalog.PageRender;
import org.wolm.catalog.RenderFactory;
import org.wolm.series.Series;
import org.wolm.series.SeriesUrlRender;
import org.wolm.weebly.WeeblyPage;

public class CatalogSeriesIndexPageRender extends PageRender {
	private final Catalog catalog;

	public CatalogSeriesIndexPageRender(Catalog catalog) {
		this.catalog = catalog;
	}

	@Override
	public String getSkinName() {
		return "basic";
	}

	@Override
	public void render(File pageFile) throws Exception {
		System.out.println("Writing series index to file " + pageFile.getName() + "…");

		// read the Weebly template page
		WeeblyPage page = preparePage();

		// create a map of content to substitute
		Map<String, String> content = prepareContent("Word of Life Ministries Online Series");
		content.put("Content", getSeriesIndexHtml());

		// insert the content
		page.substituteVariables(content);

		// write the page out
		try (PrintStream outStream = new PrintStream(new FileOutputStream(pageFile))) {
			page.printPage(outStream);
		}

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
