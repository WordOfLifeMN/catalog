package org.wolm.catalog;

import java.net.URL;

/**
 * Factory to generate rendering objects
 * 
 * @author wolm
 */
public class RenderFactory {

	/** Stores the skin currently to use when loading all templates. <code>null</code> is default skin */
	private static String skin = null;

	/** Stores the default page template */
	private static URL pageTemplateUrl = null;

	/** Initialize the static fields */
	static {
		setSkin(null);
	}

	public static String getSkin() {
		return skin;
	}

	/**
	 * Sets the skin to use when generating all renders. This can affect the weebly page template and the names of all
	 * the freemarker templates.
	 * 
	 * @param skin Name of the skin to use. If <code>null</code> (the default), then all templates will be loaded
	 * unmodified. If not <code>null</code>, then the skin name will be appended to all template names with an
	 * underscore. For instance, if you try to load "template", then instead of "template.ftl", we will load
	 * "template_skin.ftl"
	 */
	public static void setSkin(String skin) {
		RenderFactory.skin = skin;

		try {
			if (skin == null) {
				setWeeblyPageTemplateUrl(new URL("http://www.wordoflifemn.org/media-catalog.html"));
			}
		}
		catch (Exception e) {
			// impossible
		}
	}

	/**
	 * Gets the Weebly page template for the specified skin
	 * 
	 * @param skin
	 * @return
	 */
	public static URL getWeeblyPageTemplateUrl() {
		return pageTemplateUrl;
	}

	/**
	 * Sets the default page template
	 * 
	 * @param pageTemplateUrl
	 */
	public static void setWeeblyPageTemplateUrl(URL pageTemplateUrl) {
		RenderFactory.pageTemplateUrl = pageTemplateUrl;
	}

	/**
	 * Gets the full name of the template to load, including any skin modifications
	 */
	public static String getFullTemplateName(String templateName) {
		if (skin == null) return templateName + ".ftl";
		return templateName + "_" + skin + ".ftl";
	}

}
