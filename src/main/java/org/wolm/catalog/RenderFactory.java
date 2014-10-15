package org.wolm.catalog;

import java.net.URL;

import org.wolm.catalog.catalog.Catalog;
import org.wolm.catalog.catalog.CatalogSeriesIndexPageRender;
import org.wolm.series.Series;
import org.wolm.series.SeriesFullHtmlRender;
import org.wolm.series.SeriesPageRender;
import org.wolm.series.SeriesSummaryHtmlRender;

/**
 * Factory to generate rendering objects
 * 
 * @author wolm
 */
public class RenderFactory {

	/** Stores the default page template */
	private static URL pageTemplateUrl;
	static {
		try {
			pageTemplateUrl = new URL("http://www.wordoflifemn.org/media-catalog.html");
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
	public static URL getWeeblyPageTemplateUrl(String skin) {
		return pageTemplateUrl;
	}

	/**
	 * Sets the default page template
	 * 
	 * @param pageTemplateUrl
	 */
	public static void setPageTemplateUrl(URL pageTemplateUrl) {
		RenderFactory.pageTemplateUrl = pageTemplateUrl;
	}

	/**
	 * Gets the page render for a catalog
	 * 
	 * @param skin
	 * @param templateUrl
	 * @param catalog
	 * @return
	 */
	public static PageRender getPageRender(String skin, Catalog catalog) {
		PageRender render = null;

		switch (skin) {
		case "basic":
			render = new CatalogSeriesIndexPageRender(catalog);
			break;
		}

		// return a validated render
		if (render != null) {
			validateRender(skin, render);
			return render;
		}

		// return the default
		System.out.println("WARNING: Unsupported skin '" + skin + "' for catalog, using default.");
		return new CatalogSeriesIndexPageRender(catalog);
	}

	/**
	 * Gets the page render for a series
	 * 
	 * @param skin
	 * @param templateUrl
	 * @param series
	 * @return
	 */
	public static PageRender getPageRender(String skin, Series series) {
		PageRender render = null;

		switch (skin) {
		case "basic":
			render = new SeriesPageRender(series);
			break;
		}

		// return a validated render
		if (render != null) {
			validateRender(skin, render);
			return render;
		}

		// return the default
		System.out.println("WARNING: Unsupported skin '" + skin + "' for series, using default.");
		return new SeriesPageRender(series);
	}

	/**
	 * Gets a html render for a series
	 * 
	 * @param skin
	 * @param series
	 * @return
	 */
	public static HtmlRender getHtmlRender(String skin, Series series) {
		HtmlRender render = null;

		switch (skin) {
		case "basic-summary":
			render = new SeriesSummaryHtmlRender(series);
			break;
		case "basic-full":
			render = new SeriesFullHtmlRender(series);
			break;
		}

		// return a validated
		if (render != null) {
			validateRender(skin, render);
			return render;
		}

		// return a default render
		System.out.println("WARNING: Unsupported skin '" + skin + "' for series, using default.");
		return new SeriesSummaryHtmlRender(series);

	}

	/**
	 * Validate the the specified render matches the requested skin
	 * 
	 * @param skin
	 * @param render
	 */
	private static void validateRender(String skin, Render render) {
		if (render.getSkinName().equals(skin)) return;

		throw new IllegalStateException("RenderFactory generated a '" + render.getSkinName() + "' render when a '"
				+ skin + "' render was requested.");

	}
}
