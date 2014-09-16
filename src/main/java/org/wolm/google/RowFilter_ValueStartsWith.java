package org.wolm.google;

import javax.annotation.Nonnull;

public class RowFilter_ValueStartsWith extends RowFilter {

	@Nonnull
	private final String columnName;
	@Nonnull
	private final String targetPrefix;

	public RowFilter_ValueStartsWith(@Nonnull String columnName, @Nonnull String targetValue) {
		super();
		this.columnName = columnName;
		this.targetPrefix = targetValue;
	}

	@Override
	public boolean keepRow(GoogleRow row) {
		String value = row.getValue(columnName);
		if (value == null) return false;
		return value.startsWith(targetPrefix);
	}
}
