package org.wolm.google;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

public class GoogleSpreadsheet {

	private final GoogleHelper helper;
	private final SpreadsheetEntry spreadsheet;

	// cached
	private List<GoogleWorksheet> worksheets = null;

	public GoogleSpreadsheet(@Nonnull GoogleHelper helper, @Nonnull SpreadsheetEntry spreadsheet) {
		super();
		this.helper = helper;
		this.spreadsheet = spreadsheet;
	}

	public GoogleHelper getHelper() {
		return helper;
	}

	public SpreadsheetEntry getSpreadsheet() {
		return spreadsheet;
	}

	public String getTitle() {
		return spreadsheet.getTitle().getPlainText();
	}

	/** Causes all caches to be dumped and new data is read from the server */
	public void refresh() {
		worksheets = null;
	}

	/**
	 * @return All worksheets (tabs) in this spreadsheet
	 * @throws IOException
	 * @throws ServiceException
	 */
	public List<GoogleWorksheet> getWorksheets() throws IOException, ServiceException {
		if (worksheets == null) {
			List<WorksheetEntry> entries = spreadsheet.getWorksheets();
			worksheets = new ArrayList<>(entries.size());
			for (WorksheetEntry entry : entries) {
				worksheets.add(new GoogleWorksheet(this, entry));
			}
		}
		return worksheets;

	}

	/**
	 * @param name Title of a worksheet
	 * @return The worksheet with the specified title, or <code>null</code> if no worksheet with that title is present
	 * @throws IOException
	 * @throws ServiceException
	 */
	public GoogleWorksheet getWorksheet(@Nonnull String name) throws IOException, ServiceException {
		for (GoogleWorksheet worksheet : getWorksheets()) {
			if (name.equals(worksheet.getTitle())) return worksheet;
		}
		return null;
	}
}
