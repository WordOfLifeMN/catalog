package org.wolm.catalog.environment;

import javax.annotation.Nonnull;

import org.wolm.message.Message;
import org.wolm.series.Series;

/** Filters for series that happened at any point in the year */
public class IntersectingWithYearFilter extends YearFilter {

	public IntersectingWithYearFilter(int year) {
		super(year);
	}

	@Override
	public boolean shouldInclude(@Nonnull Series series) {
		// optimize by checking if the series started or ended in the year
		if (series.getStartDate() != null && isDateInYear(series.getStartDate())) return true;
		if (series.getEndDate() != null && isDateInYear(series.getEndDate())) return true;

		// we don't have a greater than or less than option, so check each message
		for (Message message : series.getMessages())
			if (isMessageInYear(message)) return true;

		// no part of the series intersected the year
		return false;
	}

}
