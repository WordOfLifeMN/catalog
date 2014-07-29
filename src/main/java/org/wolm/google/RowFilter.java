package org.wolm.google;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class RowFilter {

	/**
	 * Filters a list of rows by applying a filter to each row in the list on discarding any that the filter rejects.
	 * 
	 * @param candidateRows A list of rows that are the candidates that need filtered
	 * @param filter Filter to apply. <code>null</code> applies no filter, but still makes a copy of the input list.
	 * @return A copy of the input list with rejected rows removed
	 */
	@Nonnull
	public static List<GoogleRow> filter(@Nonnull List<GoogleRow> candidateRows, @Nullable RowFilter filter) {
		List<GoogleRow> rows = new ArrayList<>(candidateRows.size());

		if (filter != null) {
			for (GoogleRow row : candidateRows)
				if (filter.keepRow(row)) rows.add(row);
		}

		return rows;
	}

	/**
	 * Determines whether a row should be kept in the list when filtering.
	 * 
	 * @param row A row to evaluate
	 * @return <code>true</code> if the row should be kept, <code>false</code> if it should be rejected
	 */
	public abstract boolean keepRow(GoogleRow row);

}
