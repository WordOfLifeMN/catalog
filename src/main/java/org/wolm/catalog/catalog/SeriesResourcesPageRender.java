package org.wolm.catalog.catalog;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.wolm.catalog.PageRender;
import org.wolm.series.Series;

public class SeriesResourcesPageRender extends PageRender {

	public SeriesResourcesPageRender(List<Series> seriesWithResources) {
		super("resources");
		setSeriesWithResources(seriesWithResources);
	}

	public List<Series> getSeriesWithResources() {
		@SuppressWarnings("unchecked")
		List<Series> seriesList = (List<Series>) getDataFromModel("seriesList");

		if (seriesList == null) return Collections.emptyList();
		return Collections.unmodifiableList(seriesList);
	}

	private void setSeriesWithResources(List<Series> seriesWithResources) {
		addDataToModel("seriesList", seriesWithResources);
	}

	@Override
	public void render(File pageFile) throws Exception {
		System.out.println("Writing resources to file " + pageFile.getName() + "…");

		super.render(pageFile);
	}
}
