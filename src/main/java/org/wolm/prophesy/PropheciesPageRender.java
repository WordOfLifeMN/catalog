package org.wolm.prophesy;

import java.io.File;
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

	@Override
	public void render(File pageFile) throws Exception {
		App.logInfo("Writing page for prophecy index to file " + pageFile.getName() + " ...");

		// create the index page
		super.render(pageFile);

		// create all the supporting pages (for each individual prophecy)
		File pageDirectory = pageFile.getParentFile();
		for (Prophecy prophecy : getProphecyList()) {
			ProphecyPageRender prophecyRender = new ProphecyPageRender(prophecy);
			File prophecyFile = new File(pageDirectory, "prophecy-" + prophecy.getId() + ".html");
			prophecyRender.render(prophecyFile);
		}
	}
}
