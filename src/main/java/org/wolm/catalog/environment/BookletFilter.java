package org.wolm.catalog.environment;

import javax.annotation.Nonnull;

import org.wolm.message.Message;
import org.wolm.series.Series;

public class BookletFilter implements MediaCatalogFilter {
	/** {@code true} to only include booklets, {@code false} to only include non-booklets */
	private final boolean isBooklet;

	/**
	 * Constructor
	 * 
	 * @param isBooklet {@code true} to only include booklets, {@code false} to only include non-booklets
	 */
	public BookletFilter(boolean isBooklet) {
		this.isBooklet = isBooklet;
	}

	public boolean shouldInclude(@Nonnull Series series) {
		return series.isBooklet() == isBooklet;
	}

	/** Messages are never booklets */
	public boolean shouldInclude(@Nonnull Message message) {
		return true;
	}

	public boolean shouldInclude(@Nonnull Series series, @Nonnull Message message) {
		return shouldInclude(series);
	}

}
