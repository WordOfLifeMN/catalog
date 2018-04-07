package org.wolm.catalog.environment;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.wolm.message.Message;
import org.wolm.series.Series;

/**
 * A filter that only allows content of a particular type to be included. Useful for building a catalog for Messages vs.
 * Testimonies vs. C.O.R.E., etc
 * <p>
 * Series must contain at least one message of this type. Messages must be exactly this type. Messages in series are
 * treated just like messages.
 * <p>
 * Note that this will never include messages without a type.
 * 
 * @author wolm
 */
public class TypeFilter implements MediaCatalogFilter {
	private List<String> includedTypes = new ArrayList<>();
	private List<String> excludedTypes = new ArrayList<>();

	public TypeFilter with(String... types) {
		for (String type : types)
			includedTypes.add(type);
		return this;
	}

	public TypeFilter without(String... types) {
		for (String type : types)
			excludedTypes.add(type);
		return this;
	}

	public boolean shouldInclude(@Nonnull Series series) {
		for (Message message : series.getMessages())
			if (shouldInclude(message)) return true;
		return false;
	}

	public boolean shouldInclude(@Nonnull Message message) {
		if (message.getType() == null) return false;

		// if there are included types then it must be one of them
		if (!includedTypes.isEmpty() && includedTypes.contains(message.getType())) return true;

		// if there are excluded types then it must not be one of them
		if (!excludedTypes.isEmpty() && !excludedTypes.contains(message.getType())) return true;

		return false;
	}

	public boolean shouldInclude(@Nonnull Series series, @Nonnull Message message) {
		return shouldInclude(message);
	}

}
