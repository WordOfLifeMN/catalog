package org.wolm.catalog.environment;

import javax.annotation.Nonnull;

import org.wolm.series.Series;

/** Filters for series that started in the year regardless of when they finished */
public class StartedWithinYearFilter extends YearFilter {

	public StartedWithinYearFilter(int year) {
		super(year);
	}

	@Override
	public boolean shouldInclude(@Nonnull Series series) {
		return series.getStartDate() != null && isDateInYear(series.getStartDate());
	}

}
