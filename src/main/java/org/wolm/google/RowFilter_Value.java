package org.wolm.google;

import javax.annotation.Nonnull;

public class RowFilter_Value extends RowFilter {

	@Nonnull
	private final String columnName;
	@Nonnull
	private final String targetValue;

	public RowFilter_Value(@Nonnull String columnName, @Nonnull String targetValue) {
		super();
		this.columnName = columnName;
		this.targetValue = targetValue;
	}

	@Override
	public boolean keepRow(GoogleRow row) {
		return targetValue.equals(row.getValue(columnName));
	}

}
