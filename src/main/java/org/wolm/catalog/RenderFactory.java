package org.wolm.catalog;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Factory to generate rendering objects
 * 
 * @author wolm
 */
public class RenderFactory {

	/** The skin currently to use when loading all templates. <code>null</code> is default skin */
	private static String skin = null;

	/** The page template */
	private static URL weeblyPageTemplateUrl = null;

	/** The base reference path for all internally referenced pages */
	private static String baseRef = null;

	/** Minimum level of visibility that should be output */
	private static AccessLevel minVisibility;

	/** List of all pages that have been created */
	private static Set<File> createdPages = new HashSet<>();

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
		return weeblyPageTemplateUrl;
	}

	/**
	 * Sets the default page template
	 * 
	 * @param pageTemplateUrl
	 */
	public static void setWeeblyPageTemplateUrl(URL pageTemplateUrl) {
		RenderFactory.weeblyPageTemplateUrl = pageTemplateUrl;
	}

	/**
	 * Gets the full name of the template to load, including any skin modifications
	 */
	public static String getFullTemplateName(String templateName) {
		if (skin == null) return templateName + ".ftl";
		return templateName + "_" + skin + ".ftl";
	}

	public static String getBaseRef() {
		return baseRef;
	}

	public static void setBaseRef(String baseRef) {
		RenderFactory.baseRef = baseRef;
	}

	public static AccessLevel getMinVisibility() {
		return minVisibility;
	}

	public static void setMinVisibility(AccessLevel minVisibility) {
		RenderFactory.minVisibility = minVisibility;
	}

	/**
	 * Determines if the specified item is visible under the current visibilty settings
	 * 
	 * @param itemVisibility Visibility of item in question
	 * @return <code>true</code> if the item is visible (can be displayed), <code>false</code> if the item should be
	 * hidden
	 */
	public static boolean isVisible(AccessLevel itemVisibility) {
		AccessLevel effectiveItemVisibility = itemVisibility == null ? AccessLevel.PRIVATE : itemVisibility;

		switch (minVisibility) {
		case RAW:
			return true;
		case PRIVATE:
			switch (effectiveItemVisibility) {
			case RAW:
				return false;
			default:
				return true;
			}
		case PROTECTED:
			switch (effectiveItemVisibility) {
			case RAW:
			case PRIVATE:
				return false;
			default:
				return true;
			}
		case PUBLIC:
			switch (effectiveItemVisibility) {
			case RAW:
			case PRIVATE:
			case PROTECTED:
				return false;
			default:
				return true;
			}
		default:
			return false;
		}
	}

	public static Set<File> getCreatedPages() {
		return createdPages;
	}

	public static void setCreatedPages(Set<File> createdPages) {
		RenderFactory.createdPages = createdPages;
	}

	public static void addCreatedPage(File createdPage) {
		createdPages.add(createdPage);
	}

}
