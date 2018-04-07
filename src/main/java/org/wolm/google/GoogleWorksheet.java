package org.wolm.google;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.ObjectUtils;

import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class GoogleWorksheet {

	private final GoogleSpreadsheet spreadsheet;
	private final WorksheetEntry worksheet;

	// cached
	List<GoogleRow> rows = null;
	List<String> columnNames = null;

	public GoogleWorksheet(GoogleSpreadsheet spreadsheet, WorksheetEntry worksheet) {
		super();
		this.spreadsheet = spreadsheet;
		this.worksheet = worksheet;
	}

	private GoogleHelper getHelper() {
		return spreadsheet.getHelper();
	}

	public GoogleSpreadsheet getSpreadsheet() {
		return spreadsheet;
	}

	public WorksheetEntry getWorksheet() {
		return worksheet;
	}

	public String getTitle() {
		return worksheet.getTitle().getPlainText();
	}

	public int getColumnCount() {
		return worksheet.getColCount();
	}

	public int getRowCount() {
		return worksheet.getRowCount();
	}

	public boolean getCanEdit() {
		return worksheet.getCanEdit();
	}

	/** Causes all caches to be dumped and new data is read from the server */
	public void refresh() {
		rows = null;
		columnNames = null;
	}

	public List<String> getColumnNames() throws IOException, ServiceException {
		// use a column name cache for consistency, since Google's data is in a set, which is unordered. this way the
		// ordering is undefined, but consistent
		if (columnNames == null) {
			if (!hasRow(0)) return Collections.emptyList();
			columnNames = new ArrayList<>(getRow(0).getColumnNames());
		}
		return columnNames;
	}

	public boolean hasColumn(String columnName) {
		try {
			return getColumnNames().contains(columnName);
		}
		catch (Exception e) {
			return false;
		}
	}

	public boolean hasRow(int rowIndex) {
		try {
			getRow(rowIndex);
			return true;
		}
		catch (IOException | ServiceException e) {
			return false;
		}
	}

	public boolean hasCell(int rowIndex, String columnName) {
		try {
			GoogleRow row = getRow(rowIndex);
			return row == null ? false : row.hasColumn(columnName);
		}
		catch (IOException | ServiceException e) {
			return false;
		}
	}

	/**
	 * @return List of row data for the worksheet
	 * @throws AuthenticationException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws URISyntaxException
	 */
	@Nonnull
	public List<GoogleRow> getRows() throws IOException, ServiceException {
		if (rows == null) {
			URL listFeedUrl = worksheet.getListFeedUrl();
			ListFeed listFeed = getHelper().getService().getFeed(listFeedUrl, ListFeed.class);
			List<ListEntry> entries = listFeed.getEntries();
			rows = new ArrayList<>(entries.size());
			for (ListEntry entry : entries)
				rows.add(new GoogleRow(this, entry));
		}
		return rows;
	}

	/**
	 * @param columnName Name of the column to order by
	 * @return List of row data for the worksheet, ordered by the specified column. <code>null</code> if the requested
	 * column cannot be found
	 * @throws AuthenticationException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws URISyntaxException
	 */
	@Nullable
	public List<GoogleRow> getRowsOrderedBy(@Nonnull final String columnName)
			throws AuthenticationException, IOException, ServiceException, URISyntaxException {
		return getRowsOrderedBy(columnName, true);
	}

	/**
	 * @param columnName Name of the column to order by
	 * @param <code>true</code> if the column should be ordered ascending, <code>false</code> if descending
	 * @return List of row data for the worksheet, ordered by the specified column ordered as requested.
	 * <code>null</code> if the requested column cannot be found
	 * @throws AuthenticationException
	 * @throws IOException
	 * @throws ServiceException
	 * @throws URISyntaxException
	 */
	@Nullable
	public List<GoogleRow> getRowsOrderedBy(@Nonnull final String columnName, final boolean ascending)
			throws AuthenticationException, IOException, ServiceException, URISyntaxException {
		List<GoogleRow> rows = getRows();
		if (rows.isEmpty()) return rows;
		if (!rows.get(0).hasColumn(columnName)) return null;

		Collections.sort(rows, new Comparator<GoogleRow>() {
			public int compare(GoogleRow row1, GoogleRow row2) {
				String value1 = row1.getValue(columnName);
				String value2 = row2.getValue(columnName);
				return ObjectUtils.compare(value1, value2, true) * (ascending ? 1 : -1);
			}
		});

		return rows;
	}

	/**
	 * @param index Base-0 index of a row
	 * @return The row with the specified index. <code>null</code> if index is out of range
	 * @throws ServiceException
	 * @throws IOException
	 * @throws AuthenticationException
	 */
	public GoogleRow getRow(int index) throws IOException, ServiceException {
		if (index < 0) return null;
		List<GoogleRow> rows = getRows();
		return index >= rows.size() ? null : rows.get(index);
	}

	/**
	 * Gets the value of a cell. Note this cannot be used to differentiate between a cell that is not legal, and a cell
	 * that has no value as both return <code>null</code>. Use {@link #hasCell(int,String)} if the difference is
	 * important
	 * 
	 * @param index Base-0 index of a row
	 * @param columnName Name of a column
	 * @return The value of the cell with the specified index and column name. <code>null</code> if index is out of
	 * range or column doesn't exist
	 * @throws ServiceException
	 * @throws IOException
	 * @throws AuthenticationException
	 */
	public String getCell(int rowIndex, String columnName) throws IOException, ServiceException {
		GoogleRow row = getRow(rowIndex);
		return row == null ? null : row.getValue(columnName);
	}

	/**
	 * Gets the value of a cell as a long. Note this cannot be used to differentiate between a cell that is not legal,
	 * and a cell that has no value as both return <code>null</code>. Use {@link #hasCell(int,String)} if the difference
	 * is important.
	 * 
	 * @param index Base-0 index of a row
	 * @param columnName Name of a column
	 * @return The value of the cell with the specified index and column name. <code>null</code> if index is out of
	 * range or column doesn't exist or cell does not contain a number
	 * @throws ServiceException
	 * @throws IOException
	 * @throws AuthenticationException
	 */
	public Long getCellAsLong(int rowIndex, String columnName) throws IOException, ServiceException {
		GoogleRow row = getRow(rowIndex);
		return row == null ? null : row.getLongValue(columnName);
	}

	/**
	 * Gets the value of a cell as a double. Note this cannot be used to differentiate between a cell that is not legal,
	 * and a cell that has no value as both return <code>null</code>. Use {@link #hasCell(int,String)} if the difference
	 * is important.
	 * 
	 * @param index Base-0 index of a row
	 * @param columnName Name of a column
	 * @return The value of the cell with the specified index and column name. <code>null</code> if index is out of
	 * range or column doesn't exist or cell does not contain a number
	 * @throws ServiceException
	 * @throws IOException
	 * @throws AuthenticationException
	 */
	public Double getCellAsDouble(int rowIndex, String columnName) throws IOException, ServiceException {
		GoogleRow row = getRow(rowIndex);
		return row == null ? null : row.getDoubleValue(columnName);
	}

	/**
	 * Gets the value of a cell as a date. Note this cannot be used to differentiate between a cell that is not legal,
	 * and a cell that has no value as both return <code>null</code>. Use {@link #hasCell(int,String)} if the difference
	 * is important.
	 * 
	 * @param index Base-0 index of a row
	 * @param columnName Name of a column
	 * @return The value of the cell with the specified index and column name. <code>null</code> if index is out of
	 * range or column doesn't exist or cell does not contain a number
	 * @throws ServiceException
	 * @throws IOException
	 * @throws AuthenticationException
	 */
	public Date getCellAsDate(int rowIndex, String columnName) throws IOException, ServiceException {
		GoogleRow row = getRow(rowIndex);
		return row == null ? null : row.getDateValue(columnName);
	}

}
