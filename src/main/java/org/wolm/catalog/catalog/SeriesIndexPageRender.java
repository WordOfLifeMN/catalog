package org.wolm.catalog.catalog;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.wolm.catalog.AccessLevel;
import org.wolm.catalog.PageRender;
import org.wolm.series.Series;
import org.wolm.series.SeriesPageRender;
import org.wolm.series.SeriesUrlRender;

public class SeriesIndexPageRender extends PageRender {

	public SeriesIndexPageRender(List<Series> seriesList) {
		super("series-index");
		setSeriesList(seriesList);
	}

	public List<Series> getSeriesList() {
		@SuppressWarnings("unchecked")
		List<Series> seriesList = (List<Series>) getDataFromModel("seriesList");

		if (seriesList == null) return Collections.emptyList();
		return Collections.unmodifiableList(seriesList);
	}

	private void setSeriesList(List<Series> seriesList) {
		addDataToModel("seriesList", seriesList);
	}

	public String getIndexTitle() {
		return (String) getDataFromModel("title");
	}

	public void setIndexTitle(String indexTitle) {
		addDataToModel("title", indexTitle);
	}

	public String getIndexDescription() {
		return (String) getDataFromModel("description");
	}

	public void setIndexDescription(String indexDescription) {
		addDataToModel("description", indexDescription);
	}

	@Override
	public void render(File pageFile) throws Exception {
		System.out.println("Writing series index to file '" + pageFile.getName() + "'…");

		super.render(pageFile);

		// write out supporting files (i.e. all the series pages)
		File pageDirectory = pageFile.getParentFile();
		for (Series series : getSeriesList()) {
			if (series.getVisibility() != AccessLevel.PUBLIC) {
				System.out.println("Skpping non-Public series " + series.getTitle());
				continue;
			}
			PageRender seriesRender = new SeriesPageRender(series);
			File seriesFile = new File(pageDirectory, new SeriesUrlRender(series).getFileName());
			seriesRender.render(seriesFile);
		}

	}
}
