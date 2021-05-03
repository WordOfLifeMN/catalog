package org.wolm.catalog.catalog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.wolm.catalog.App;
import org.wolm.catalog.PageRender;
import org.wolm.catalog.environment.RenderEnvironment;
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

	public String getIndexDescription() {
		return (String) getDataFromModel("description");
	}

	public void setIndexDescription(String indexDescription) {
		addDataToModel("description", indexDescription);
	}

	public String getPageBaseName() {
		return (String) getDataFromModel("pageBaseName");
	}

	public void setPageBaseName(String pageName) {
		addDataToModel("pageBaseName", pageName);
	}

	public void setPageName(String pageName) {
		addDataToModel("pageName", pageName);
	}

	@Override
	public void render(File pageFile) throws Exception {
		App.logInfo("Writing series index to file '" + pageFile.getName() + "' ...");

		try {
			App.logIndent();

			setPageBaseName(pageFile.getName());

			// render the default by title
			setPageName(pageFile.getName());
			super.render(pageFile);

			// render sorted by date ascending
			List<Series> seriesList = new ArrayList<>(getSeriesList());
			Collections.sort(seriesList, Series.byDate);
			setSeriesList(seriesList);
			String pageName = pageFile.getName().replace(".html", "-09.html");
			setPageName(pageName);
			File renderFile = new File(pageFile.getParentFile(), pageName);
			super.render(renderFile);

			// render sorted by date descending
			seriesList = new ArrayList<>(getSeriesList());
			Collections.sort(seriesList, Series.byDateDescending);
			setSeriesList(seriesList);
			pageName = pageFile.getName().replace(".html", "-90.html");
			setPageName(pageName);
			renderFile = new File(pageFile.getParentFile(), pageName);
			super.render(renderFile);

			// write out supporting files (i.e. all the series pages)
			File pageDirectory = pageFile.getParentFile();
			for (Series series : getSeriesList()) {
				if (!RenderEnvironment.instance().shouldInclude(series)) {
					App.logInfo("Filtering out series " + series.getTitle());
					continue;
				}
				SeriesPageRender seriesRender = new SeriesPageRender(series);
				seriesRender.setMinistry(getMinistry());
				File seriesFile = new File(pageDirectory, new SeriesUrlRender(series).getFileName());
				seriesRender.render(seriesFile);
			}
		}
		finally {
			App.logOutdent();
		}
	}
}
