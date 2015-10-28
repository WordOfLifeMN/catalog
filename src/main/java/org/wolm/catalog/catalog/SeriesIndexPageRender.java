package org.wolm.catalog.catalog;

import java.io.File;
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

	public String getDepartment() {
		return (String) getDataFromModel("department");
	}

	public void setDepartment(String department) {
		addDataToModel("department", department);
	}

	@Override
	public void render(File pageFile) throws Exception {
		App.logInfo("Writing series index to file '" + pageFile.getName() + "' ...");

		try {
			App.logIndent();

			super.render(pageFile);

			// write out supporting files (i.e. all the series pages)
			File pageDirectory = pageFile.getParentFile();
			for (Series series : getSeriesList()) {
				if (!RenderEnvironment.instance().shouldInclude(series)) {
					App.logInfo("Filtering out series " + series.getTitle());
					continue;
				}
				SeriesPageRender seriesRender = new SeriesPageRender(series);
				seriesRender.setDepartment(getDepartment());
				File seriesFile = new File(pageDirectory, new SeriesUrlRender(series).getFileName());
				seriesRender.render(seriesFile);
			}
		}
		finally {
			App.logOutdent();
		}
	}
}
