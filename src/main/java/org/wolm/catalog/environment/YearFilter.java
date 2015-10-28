package org.wolm.catalog.environment;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.annotation.Nonnull;

import org.wolm.message.Message;
import org.wolm.series.Series;

/**
 * Filter to determine if a message or series should be in the catalog based on the year the series or message was
 * recorded.
 * 
 * @author wolm
 */
public abstract class YearFilter implements CatalogFilter {
	/** Year being filtered on */
	private final int year;

	/** Construct with the filtered year */
	protected YearFilter(int year) {
		this.year = year;
	}

	public abstract boolean shouldInclude(@Nonnull Series series);

	public boolean shouldInclude(@Nonnull Message message) {
		return isMessageInYear(message);
	}

	public boolean shouldInclude(@Nonnull Series series, @Nonnull Message message) {
		return shouldInclude(series);
	}

	protected boolean isMessageInYear(@Nonnull Message message) {
		return isDateInYear(message.getDate());
	}

	protected boolean isDateInYear(Date date) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(date);
		return cal.get(Calendar.YEAR) == year;
	}
}
