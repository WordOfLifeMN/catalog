package org.wolm.google;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.gdata.data.spreadsheet.ListEntry;

public class GoogleRow {
	private final GoogleWorksheet worksheet;
	private final ListEntry row;
	private final SimpleDateFormat gregorianDateFormatter = new SimpleDateFormat("MM/dd/yyyy");
	private final SimpleDateFormat yearMonthDayDateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	public GoogleRow(@Nonnull GoogleWorksheet worksheet, @Nonnull ListEntry row) {
		super();
		this.worksheet = worksheet;
		this.row = row;
	}

	public GoogleWorksheet getWorksheet() {
		return worksheet;
	}

	public ListEntry getRow() {
		return row;
	}

	public Set<String> getColumnNames() {
		return row.getCustomElements().getTags();
	}

	public boolean hasColumn(String name) {
		return getColumnNames().contains(name);
	}

	public String getValue(String columnName) {
		String value = row.getCustomElements().getValue(columnName);
		if (columnName.indexOf("date") != -1 && value.matches("\\d+/\\d+/\\d+")) {
			try {
				value = convertDateToYearMonthDay(value);
			}
			catch (ParseException e) {
				// ignore and just return the raw string below
			}
		}
		return value;
	}

	/**
	 * @param columnName Name of column to get
	 * @return Value of the column as a long. If the column is a floating point, then it is converted to an long.
	 * <code>null</code> if the column data is not a number or the column cannot be found.
	 */
	public Long getLongValue(String columnName) {
		String value = getValue(columnName);
		if (value == null) return null;

		// try as integer
		try {
			return Long.parseLong(value);
		}
		catch (NumberFormatException e) {
			// ignore
		}

		// try as floating point
		try {
			return Long.valueOf((long) Double.parseDouble(value));
		}
		catch (NumberFormatException e) {
			// ignore
		}

		// must not be a number
		return null;
	}

	/**
	 * @param columnName Name of column to get
	 * @return Value of the column as a double. <code>null</code> if the column data is not a number or the column
	 * cannot be found.
	 */
	public Double getDoubleValue(String columnName) {
		String value = getValue(columnName);
		if (value == null) return null;

		// try as integer
		try {
			return Double.valueOf(Long.parseLong(value));
		}
		catch (NumberFormatException e) {
			// ignore
		}

		// try as floating point
		try {
			return Double.parseDouble(value);
		}
		catch (NumberFormatException e) {
			// ignore
		}

		// must not be a number
		return null;
	}

	/**
	 * @param columnName Name of column to get
	 * @return Value of the column as a date. <code>null</code> if the column data is not a date or the column cannot be
	 * found.
	 */
	public Date getDateValue(String columnName) {
		String value = getValue(columnName);
		if (value == null) return null;

		if (!value.matches("\\d+-\\d+-\\d+")) return null;

		try {
			return yearMonthDayDateFormatter.parse(value);
		}
		catch (ParseException e) {
			// ignore
		}

		// must not be a number
		return null;
	}

	private String convertDateToYearMonthDay(String value) throws ParseException {
		return yearMonthDayDateFormatter.format(gregorianDateFormatter.parse(value));
	}
}
