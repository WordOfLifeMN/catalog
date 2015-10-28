package org.wolm.catalog.environment;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.annotation.Nonnull;

import org.wolm.message.Message;
import org.wolm.series.Series;

public class RecentFilter implements CatalogFilter {
	/** Earliest date we will accept as qualifying for "recent" */
	private Date cutoff;

	/**
	 * Sets the cutoff date which defines what "recent" means.
	 * 
	 * @param cutoff Only series and messages that end after this date are included
	 * @return This object
	 */
	public RecentFilter withCutoff(Date cutoff) {
		this.cutoff = cutoff;
		return this;
	}

	/**
	 * Sets the number of days back "recent" means
	 * 
	 * @param days Only series and messages that finish within this number of days are included
	 * @return This object
	 */
	public RecentFilter withDays(int days) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.add(Calendar.DATE, -1 * days);
		return withCutoff(cal.getTime());
	}

	public boolean shouldInclude(@Nonnull Series series) {
		return series.getEndDate() == null || !series.getEndDate().before(cutoff);
	}

	public boolean shouldInclude(@Nonnull Message message) {
		return message.getDate() != null && !message.getDate().before(cutoff);
	}

	public boolean shouldInclude(@Nonnull Series series, @Nonnull Message message) {
		return shouldInclude(series);
	}

}
