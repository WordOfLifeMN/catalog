package org.wolm.prophesy;

import java.io.File;

import org.wolm.catalog.App;
import org.wolm.catalog.PageRender;

public class ProphecyPageRender extends PageRender {

	public ProphecyPageRender(Prophecy prophecy) {
		super("prophecy");
		setProphecy(prophecy);
		setTitle(prophecy.getTitle());
	}

	public Prophecy getProphecy() {
		return (Prophecy) getDataFromModel("prophecy");
	}

	private void setProphecy(Prophecy prophecy) {
		addDataToModel("prophecy", prophecy);
	}

	@Override
	public void render(File pageFile) throws Exception {
		Prophecy prophecy = getProphecy();
		App.logInfo("Writing page for prophecy '" + prophecy.getTitle() + "' to file " + pageFile.getName() + " ...");

		super.render(pageFile);
	}

}
