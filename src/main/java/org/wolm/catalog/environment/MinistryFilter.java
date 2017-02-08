package org.wolm.catalog.environment;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.wolm.message.Message;
import org.wolm.series.Series;

/**
 * A filter that only allows content of a particular ministry to be included. Useful for building a catalog for
 * Messages, Testimonies, Songs, etc.
 * <p>
 * Series must contain at least one message from this ministry. Messages must be from this ministry. Messages in series
 * are treated just like messages.
 * <p>
 * Note that this will never include messages without a ministry.
 * 
 * @author wolm
 */
public class MinistryFilter implements CatalogFilter {
	private List<String> includedMinistries = new ArrayList<>();
	private List<String> excludedMinistries = new ArrayList<>();

	public MinistryFilter withMinistry(String... ministries) {
		for (String ministry : ministries)
			includedMinistries.add(ministry);
		return this;
	}

	public MinistryFilter withoutMinistry(String... ministries) {
		for (String ministry : ministries)
			excludedMinistries.add(ministry);
		return this;
	}

	public boolean shouldInclude(@Nonnull Series series) {
		for (Message message : series.getMessages())
			if (shouldInclude(message)) return true;
		return false;
	}

	public boolean shouldInclude(@Nonnull Message message) {
		if (message.getMinistry() == null) return false;

		// if there are included ministries then it must be one of them
		if (!includedMinistries.isEmpty() && includedMinistries.contains(message.getMinistry())) return true;

		// if there are excluded ministries then it must not be one of them
		if (!excludedMinistries.isEmpty() && !excludedMinistries.contains(message.getMinistry())) return true;

		return false;
	}

	public boolean shouldInclude(@Nonnull Series series, @Nonnull Message message) {
		return shouldInclude(message);
	}

}
