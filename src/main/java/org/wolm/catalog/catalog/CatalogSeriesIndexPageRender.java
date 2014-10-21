package org.wolm.catalog.catalog;

import java.io.File;

import org.wolm.catalog.PageRender;
import org.wolm.series.Series;
import org.wolm.series.SeriesPageRender;
import org.wolm.series.SeriesUrlRender;

public class CatalogSeriesIndexPageRender extends PageRender {
	private final Catalog catalog;

	public CatalogSeriesIndexPageRender(Catalog catalog) {
		super("catalog-index");
		this.catalog = catalog;
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
			PageRender seriesRender = new SeriesPageRender(series);
			File seriesFile = new File(pageDirectory, new SeriesUrlRender(series).getFileName());
			seriesRender.render(seriesFile);
		}

	}

}
