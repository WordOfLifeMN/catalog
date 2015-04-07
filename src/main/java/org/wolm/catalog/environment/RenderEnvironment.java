package org.wolm.catalog.environment;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.wolm.catalog.AccessLevel;
import org.wolm.message.Message;
import org.wolm.series.Series;

/**
 * Defines the environment that we are currently rendering. This is a singleton global under the assumption that we
 * won't need multi-threaded rendering.
 * 
 * @author wolm
 */
public class RenderEnvironment implements CatalogFilter {
	static private RenderEnvironment instance;

	/**
	 * List of filters to use to understand the current environment. Every filter must include a series or message in
	 * order for the entire environment to include the series or message
	 */
	private final List<CatalogFilter> filters = new ArrayList<>();

	// ||| kmurray - delete if not needed
	//
	// /** Minimum level of visibility that should be output */
	// private AccessLevel minVisibility;

	private RenderEnvironment() {
		super();
	}

	static public RenderEnvironment instance() {
		if (instance == null) {
			synchronized (RenderEnvironment.class) {
				if (instance == null) instance = new RenderEnvironment();
			}
		}
		return instance;
	}

	public void addFilter(CatalogFilter filter) {
		filters.add(filter);
	}

	public void clearFilters() {
		filters.clear();
	}

	public boolean shouldInclude(@Nonnull Series series) {
		for (CatalogFilter filter : filters)
			if (!filter.shouldInclude(series)) return false;
		return true;
	}

	public boolean shouldInclude(@Nonnull Message message) {
		for (CatalogFilter filter : filters)
			if (!filter.shouldInclude(message)) return false;
		return true;
	}

	public boolean shouldInclude(@Nonnull Series series, @Nonnull Message message) {
		for (CatalogFilter filter : filters)
			if (!filter.shouldInclude(series, message)) return false;
		return true;
	}

	/**
	 * @return the current visibility filter, AccessLevel.PRIVATE if none configured
	 */
	@Nonnull
	public AccessLevel getVisibility() {
		for (CatalogFilter filter : filters) {
			if (filter instanceof VisibilityFilter) return ((VisibilityFilter) filter).getVisibility();
		}
		return AccessLevel.PRIVATE;
	}

	// ||| kmurray - delete if not needed
	//
	// /**
	// * @return the current visibility level to be rendered
	// * */
	// public AccessLevel getMinVisibility() {
	// return minVisibility;
	// }
	//
	// /**
	// * Changes the visibility level of the render.
	// * <p>
	// * <b>Series</b>: Only series at this visibility will be rendered, all series of a different visibility will not
	// be
	// * rendered<br>
	// * <b>Messages</b>: Messages with a visibility of this level or higher will be displayed
	// *
	// * @param minVisibility Minimum visibility to render
	// */
	// public void setMinVisibility(AccessLevel minVisibility) {
	// this.minVisibility = minVisibility;
	// }
	//
	// public boolean shouldRender(Series s) {
	// return isExactlyVisible(s.getVisibility());
	// }
	//
	// /**
	// * Determines if the specified item is visible under the current visibility settings
	// *
	// * @param itemVisibility Visibility of item in question
	// * @return <code>true</code> if the item is visible (can be displayed), <code>false</code> if the item should be
	// * hidden
	// */
	// public boolean isAtLeastVisible(AccessLevel itemVisibility) {
	// AccessLevel effectiveItemVisibility = itemVisibility == null ? AccessLevel.PRIVATE : itemVisibility;
	//
	// switch (minVisibility) {
	// case RAW:
	// return true;
	// case PRIVATE:
	// switch (effectiveItemVisibility) {
	// case RAW:
	// return false;
	// default:
	// return true;
	// }
	// case PROTECTED:
	// switch (effectiveItemVisibility) {
	// case RAW:
	// case PRIVATE:
	// return false;
	// default:
	// return true;
	// }
	// case PUBLIC:
	// switch (effectiveItemVisibility) {
	// case RAW:
	// case PRIVATE:
	// case PROTECTED:
	// return false;
	// default:
	// return true;
	// }
	// default:
	// return false;
	// }
	// }
	//
	// /**
	// * Determines if the specified item is visible exactly at the current visibility setting
	// *
	// * @param itemVisibility Visibility of item in question
	// * @return <code>true</code> if the item is visible (can be displayed), <code>false</code> if the item should be
	// * hidden
	// */
	// public boolean isExactlyVisible(AccessLevel itemVisibility) {
	// AccessLevel effectiveItemVisibility = itemVisibility == null ? AccessLevel.PRIVATE : itemVisibility;
	//
	// return effectiveItemVisibility == minVisibility;
	// }

}
