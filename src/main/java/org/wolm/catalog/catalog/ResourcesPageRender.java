package org.wolm.catalog.catalog;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.wolm.catalog.App;
import org.wolm.catalog.NamedLink;
import org.wolm.catalog.PageRender;
import org.wolm.series.Series;

public class ResourcesPageRender extends PageRender {

	public ResourcesPageRender(List<NamedLink> resources) {
		this("resources", resources);
	}

	protected ResourcesPageRender(String templateName, List<NamedLink> resources) {
		super(templateName);
		setResources(resources);
	}

	public List<Series> getResources() {
		@SuppressWarnings("unchecked")
		List<Series> seriesList = (List<Series>) getDataFromModel("resourceList");

		if (seriesList == null) return Collections.emptyList();
		return Collections.unmodifiableList(seriesList);
	}

	private void setResources(List<NamedLink> seriesWithResources) {
		addDataToModel("resourceList", seriesWithResources);
	}

	@Override
	public void render(File pageFile) throws Exception {
		App.logInfo("Writing resources to file '" + pageFile.getName() + "' ...");

		super.render(pageFile);
	}
}
