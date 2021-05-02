package org.wolm.prophesy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.wolm.catalog.App;
import org.wolm.catalog.PageRender;

public class PropheciesPageRender extends PageRender {

	public PropheciesPageRender(List<Prophecy> prophecies) {
		super("prophecy-index");
		setProphecyList(prophecies);
	}

	public List<Prophecy> getProphecyList() {
		@SuppressWarnings("unchecked")
		List<Prophecy> prophecyList = (List<Prophecy>) getDataFromModel("prophecyList");

		if (prophecyList == null) return Collections.emptyList();
		return Collections.unmodifiableList(prophecyList);
	}

	private void setProphecyList(List<Prophecy> prophecies) {
		addDataToModel("prophecyList", prophecies);
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
		App.logInfo("Writing page for prophecy index to file " + pageFile.getName() + " ...");

		setPageBaseName(pageFile.getName());

		// render the default by title
		setPageName(pageFile.getName());
		super.render(pageFile);

		// render sorted by date ascending
		List<Prophecy> prophecies = new ArrayList<>(getProphecyList());
		Collections.sort(prophecies, Prophecy.byDate);
		setProphecyList(prophecies);
		String pageName = pageFile.getName().replace(".html", "-09.html");
		setPageName(pageName);
		File renderFile = new File(pageFile.getParentFile(), pageName);
		super.render(renderFile);

		// render sorted by date descending
		prophecies = new ArrayList<>(getProphecyList());
		Collections.sort(prophecies, Prophecy.byDateDescending);
		setProphecyList(prophecies);
		pageName = pageFile.getName().replace(".html", "-90.html");
		setPageName(pageName);
		renderFile = new File(pageFile.getParentFile(), pageName);
		super.render(renderFile);

		// create all the supporting pages (for each individual prophecy)
		File pageDirectory = pageFile.getParentFile();
		for (Prophecy prophecy : getProphecyList()) {
			ProphecyPageRender prophecyRender = new ProphecyPageRender(prophecy);
			File prophecyFile = new File(pageDirectory, "prophecy-" + prophecy.getId() + ".html");
			prophecyRender.render(prophecyFile);
		}
	}
}
