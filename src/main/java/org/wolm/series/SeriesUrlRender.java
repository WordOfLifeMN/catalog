package org.wolm.series;

import org.wolm.catalog.UrlRender;

public class SeriesUrlRender extends UrlRender {
	private final Series series;

	public SeriesUrlRender(Series series) {
		super();
		this.series = series;
	}

	@Override
	public String render() throws Exception {
		// TODO: URL encode this
		return getFileName();
	}

	public String getFileName() {
		return series.getId() + ".html";
	}

}
