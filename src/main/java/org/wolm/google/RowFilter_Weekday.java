package org.wolm.google;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RowFilter_Weekday extends RowFilter {

	private final String columnName;
	private final String weekdays[];

	/**
	 * Constructs a filter that only allows dates that fall on a certain day of the week
	 * 
	 * @param columnName Name of the column that the date should be read from
	 * @param weekdays Name(s) of the day(s) of the week to keep. These are the full day names like "Sunday" or
	 * "Wednesday"
	 */
	public RowFilter_Weekday(String columnName, String... weekdays) {
		super();
		this.columnName = columnName;
		this.weekdays = weekdays;
	}

	@Override
	public boolean keepRow(GoogleRow row) {
		SimpleDateFormat formatter = new SimpleDateFormat("EEEE");

		Date date = row.getDateValue(columnName);
		if (date == null) return false;
		String rowDay = formatter.format(date);

		for (String weekday : weekdays)
			if (weekday.equals(rowDay)) return true;

		return false;
	}

}
