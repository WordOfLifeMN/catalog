package org.wolm.catalog.environment;

import javax.annotation.Nonnull;

import org.wolm.series.Series;

/** Filters for series that fall entirely within the specified year */
public class EntirelyWithinYearFilter extends YearFilter {

	public EntirelyWithinYearFilter(int year) {
		super(year);
	}

	/** Include the series iff every message falls within the year */
	@Override
	public boolean shouldInclude(@Nonnull Series series) {
		return (series.getStartDate() != null && isDateInYear(series.getStartDate()))
				&& (series.getEndDate() == null || isDateInYear(series.getEndDate()));
	}

}
