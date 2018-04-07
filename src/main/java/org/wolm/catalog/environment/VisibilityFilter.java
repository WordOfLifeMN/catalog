package org.wolm.catalog.environment;

import javax.annotation.Nonnull;

import org.wolm.catalog.AccessLevel;
import org.wolm.message.Message;
import org.wolm.series.Series;

/**
 * A filter that only allows content of a given visibility to be included. Useful for building a catalog of Public vs
 * Protected content.
 * <p>
 * Series and Messages must be exactly of this visibility. Messages in a Series must have the Series of this visibility,
 * and the message must be the same or more visible than the series
 * 
 * @author wolm
 */
public class VisibilityFilter implements MediaCatalogFilter {

	private final AccessLevel visibilityCriteria;

	public VisibilityFilter(@Nonnull AccessLevel visibility) {
		super();
		this.visibilityCriteria = visibility;
	}

	public AccessLevel getVisibility() {
		return visibilityCriteria;
	}

	@Override
	public boolean shouldInclude(@Nonnull Series series) {
		return computeEffectiveVisibility(series.getVisibility()) == visibilityCriteria;
	}

	@Override
	public boolean shouldInclude(@Nonnull Message message) {
		return computeEffectiveVisibility(message.getVisibility()) == visibilityCriteria;
	}

	@Override
	public boolean shouldInclude(@Nonnull Series series, @Nonnull Message message) {
		if (!shouldInclude(series)) return false;

		AccessLevel seriesVis = computeEffectiveVisibility(series.getVisibility());
		AccessLevel messageVis = computeEffectiveVisibility(message.getVisibility());
		return !messageVis.isLessVisibleThan(seriesVis);
	}

	private AccessLevel computeEffectiveVisibility(@Nonnull AccessLevel visibility) {
		return visibility == null ? AccessLevel.PRIVATE : visibility;
	}
}
