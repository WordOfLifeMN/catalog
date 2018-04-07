package org.wolm.catalog.environment;

import javax.annotation.Nonnull;

import org.wolm.message.Message;
import org.wolm.series.Series;

/**
 * Defines an interface for something that can filter both series and messages for inclusion in a catalog.
 * 
 * @author wolm
 */
public interface MediaCatalogFilter {
	/**
	 * Determines if a series should be included in the current catalog
	 * 
	 * @param series Series to evaluate
	 * @return {@code true} if the series should be included/displayed in the current catalog. {@code false} if not
	 */
	public boolean shouldInclude(@Nonnull Series series);

	/**
	 * Determines if a message should be included in the current catalog. This evaluates the message independent of the
	 * series it is in (if any).
	 * 
	 * @param message Message to evaluate
	 * @return {@code true} if the message should be included/displayed in the current catalog. {@code false} if not
	 */
	public boolean shouldInclude(@Nonnull Message message);

	/**
	 * Determines if a message in a series should be included in the current catalog. This evaluates the message in the
	 * context of it's series. For some filters, messages may be included if their series is included regardless of the
	 * message values. Other filters may need to compare the message with the series (i.e. is the message more visible
	 * than the series?). A default implementation could just return {@code shouldInclude(series)} or
	 * {@code shouldInclude(message)}.
	 * 
	 * @param series Series to evaluate
	 * @param message Message to evaluate as if it were part of the {@code series}, regardless of whether it is in the
	 * series or not
	 * @return {@code true} if the message should be included/displayed in the current catalog. {@code false} if not
	 */
	public boolean shouldInclude(@Nonnull Series series, @Nonnull Message message);
}
