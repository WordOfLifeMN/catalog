package org.wolm.catalog;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.wolm.weebly.WeeblyPage;

/**
 * Base class for renderers that render weebly pages
 * 
 * @author wolm
 */
public abstract class PageRender extends Render {

	public PageRender() {
		super();
	}

	/** Render the page to the specified output file */
	public abstract void render(File outputFileName) throws Exception;

	protected WeeblyPage preparePage() throws Exception {
		// read the Weebly template page
		WeeblyPage page = new WeeblyPage(RenderFactory.getWeeblyPageTemplateUrl(getSkinName()));
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
