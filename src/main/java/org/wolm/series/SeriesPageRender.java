package org.wolm.series;

import java.io.File;

import org.wolm.catalog.App;
import org.wolm.catalog.PageRender;

public class SeriesPageRender extends PageRender {

	private final Series series;

	public SeriesPageRender(Series series) {
		super("series");
		this.series = series;
		setTitle(series.getTitle());
	}

	@Override
	public void render(File pageFile) throws Exception {
		App.logInfo("Writing page for series '" + series.getTitle() + "' to file " + pageFile.getName() + " ...");

		addDataToModel("series", series);

		super.render(pageFile);
	}

}
