package org.wolm.series;

import java.net.URL;
import java.util.Map;

import org.wolm.catalog.PageRender;
import org.wolm.weebly.WeeblyPage;

public class SeriesPageRender extends PageRender {

	private final Series series;

	public SeriesPageRender(URL templateUrl, Series series) {
		super(templateUrl);
		this.series = series;
	}

	@Override
	public WeeblyPage render() throws Exception {
		WeeblyPage page = preparePage();

		Map<String, String> content = prepareContent(series.getTitle());
		content.put("Content", new SeriesTableHtmlRender(series).render());

		page.substituteVariables(content);

		return page;
	}

}
