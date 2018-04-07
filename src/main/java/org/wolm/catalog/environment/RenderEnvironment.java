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
public class RenderEnvironment implements MediaCatalogFilter {
	static private RenderEnvironment instance;

	/**
	 * List of filters to use to understand the current environment. Every filter must include a series or message in
	 * order for the entire environment to include the series or message
	 */
	private final List<MediaCatalogFilter> filters = new ArrayList<>();

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

	public void addFilter(MediaCatalogFilter filter) {
		filters.add(filter);
	}

	public void clearFilters() {
		filters.clear();
	}

	public boolean shouldInclude(@Nonnull Series series) {
		for (MediaCatalogFilter filter : filters)
			if (!filter.shouldInclude(series)) return false;
		return true;
	}

	public boolean shouldInclude(@Nonnull Message message) {
		for (MediaCatalogFilter filter : filters)
			if (!filter.shouldInclude(message)) return false;
		return true;
	}

	public boolean shouldInclude(@Nonnull Series series, @Nonnull Message message) {
		for (MediaCatalogFilter filter : filters)
			if (!filter.shouldInclude(series, message)) return false;
		return true;
	}

	/**
	 * @return the current visibility filter, AccessLevel.PRIVATE if none configured
	 */
	@Nonnull
	public AccessLevel getVisibility() {
		for (MediaCatalogFilter filter : filters) {
			if (filter instanceof VisibilityFilter) return ((VisibilityFilter) filter).getVisibility();
		}
		return AccessLevel.PRIVATE;
	}
}
