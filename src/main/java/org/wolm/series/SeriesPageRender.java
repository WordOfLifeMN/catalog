package org.wolm.series;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Map;

import org.wolm.catalog.PageRender;
import org.wolm.weebly.WeeblyPage;

public class SeriesPageRender extends PageRender {

	private final Series series;

	public SeriesPageRender(Series series) {
		this.series = series;
	}

	@Override
	public String getSkinName() {
		return "basic";
	}

	@Override
	public void render(File pageFile) throws Exception {
		System.out.println("  Writing page for series '" + series.getTitle() + "' to file " + pageFile.getName() + "…");

		WeeblyPage page = preparePage();

		// get the content
		Map<String, String> content = prepareContent(series.getTitle());
		content.put("Content", new SeriesFullHtmlRender(series).render());

		// insert the content
		page.substituteVariables(content);

		// write the page out
		try (PrintStream outStream = new PrintStream(new FileOutputStream(pageFile))) {
			page.printPage(outStream);
		}
	}

}
