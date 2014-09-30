package org.wolm.catalog;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.wolm.weebly.WeeblyPage;

/**
 * Base class for renderers that render weebly pages
 * 
 * @author wolm
 */
public abstract class PageRender {

	protected URL templateUrl;

	public PageRender(URL templateUrl) {
		super();
		this.templateUrl = templateUrl;
	}

	public URL getWeeblyTemplatePage() {
		return templateUrl;
	}

	public void setWeeblyTemplatePage(URL templateUrl) {
		this.templateUrl = templateUrl;
	}

	public abstract WeeblyPage render() throws Exception;

	protected WeeblyPage preparePage() throws Exception {
		// read the Weebly template page
		WeeblyPage page = new WeeblyPage(templateUrl);
		page.preparePageForRemoteHosting();

		return page;
	}

	protected Map<String, String> prepareContent(String title) {
		Map<String, String> content = new HashMap<>();
		content.put("Date", new Date().toString());
		content.put("Title", title);
		return content;
	}
}
