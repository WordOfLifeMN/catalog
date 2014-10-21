package org.wolm.catalog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.wolm.weebly.WeeblyPage;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

/**
 * Base class for renderers that render weebly pages using Freemarker templates to generate the content
 * 
 * @author wolm
 */
public abstract class PageRender extends Render {

	/** The singleton configuration for all Freemarker template processing */
	private static final Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_21);
	static {
		freemarkerConfig.setClassForTemplateLoading(PageRender.class, "templates");
		freemarkerConfig.setDefaultEncoding("UTF-8");

		// Sets how errors will appear.
		freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
		// freemarkerConfig.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
	}

	private final String templateName;
	private final Map<String, Object> freemarkerDataModel = new HashMap<>();

	public PageRender(String templateName) {
		super();
		this.templateName = templateName;

		// TODO: How should we set this? can we generate it from Javascript, or is there a special markup that overrides
		// the base href?
		addDataToModel("baseRef", "file:///Users/wolm/tmp/");
	}

	protected void addDataToModel(String name, Object value) {
		freemarkerDataModel.put(name, value);
	}

	/** Render the page to the specified output file */
	public void render(File outputFile) throws Exception {
		WeeblyPage page = preparePage();

		// get the content
		Template freemarkerTemplate = freemarkerConfig.getTemplate(templateName + ".ftl");
		Writer out = new StringWriter();
		try {
			freemarkerTemplate.process(freemarkerDataModel, out);
		}
		finally {
			// closing a string writer does not have any effect, but do it for style
			out.close();
		}

		Map<String, String> content = prepareContent();
		content.put("Content", out.toString());

		// insert the content
		page.substituteVariables(content);

		// write the page out
		try (PrintStream outStream = new PrintStream(new FileOutputStream(outputFile))) {
			page.printPage(outStream);
		}
	}

	protected WeeblyPage preparePage() throws Exception {
		// read the Weebly template page
		WeeblyPage page = new WeeblyPage(RenderFactory.getWeeblyPageTemplateUrl(getSkinName()));
		page.preparePageForRemoteHosting();

		return page;
	}

	protected Map<String, String> prepareContent() {
		Map<String, String> content = new HashMap<>();
		content.put("Date", new Date().toString());
		content.put("Title", "");
		return content;
	}
}
