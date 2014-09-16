package org.wolm.google;

public class RowFilter_MaxCount extends RowFilter {

	private final int maxRowCount;
	private int currentRowIndex = 0;

	public RowFilter_MaxCount(int maxRowCount) {
		super();
		this.maxRowCount = maxRowCount;
	}

	@Override
	public boolean keepRow(GoogleRow row) {
		return (currentRowIndex++) < maxRowCount;
	}

}
