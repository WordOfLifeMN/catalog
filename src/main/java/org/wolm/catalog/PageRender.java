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
 * Base class for renderers that render Weebly pages using Freemarker templates to generate the content. You initialize
 * this class with the name of the freemarker template to use, like "herkimer". This class will calculate the name of
 * the ftl file to use based on the template name and any skin using {@link RenderFactory#getFullTemplateName(String)}
 * 
 * @author wolm
 */
public abstract class PageRender {

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

	protected PageRender(String templateName) {
		super();
		this.templateName = templateName;

		addDataToModel("baseRef", RenderFactory.getBaseRef());
	}

	/**
	 * Retrieves the current value of a model data element
	 * 
	 * @param name Name of the bean to retrieve
	 * @return Data value of the bean, <code>null</code> if none
	 */
	protected Object getDataFromModel(String name) {
		return freemarkerDataModel.get(name);
	}

	/**
	 * Adds a bean to the data model that freemarker will use to generate the page
	 * 
	 * @param name Name of the bean
	 * @param value Bean
	 */
	protected void addDataToModel(String name, Object value) {
		freemarkerDataModel.put(name, value);
	}

	/**
	 * Render the page to the specified output file
	 * 
	 * @param outputFile Output file to write the page to
	 * @throws Exception
	 */
	public void render(File outputFile) throws Exception {
		WeeblyPage page = preparePage();

		// get the content
		Template freemarkerTemplate = freemarkerConfig.getTemplate(RenderFactory.getFullTemplateName(templateName));
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

		// register the page with the factory
		RenderFactory.addCreatedPage(outputFile);
	}

	protected WeeblyPage preparePage() throws Exception {
		// read the Weebly template page
		WeeblyPage page = new WeeblyPage(RenderFactory.getWeeblyPageTemplateUrl());
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
