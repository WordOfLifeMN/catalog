package org.wolm.google;

import static org.fest.assertions.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class GoogleHelperIT {

	@Test
	public void spreadsheets() throws AuthenticationException, IOException, ServiceException {
		GoogleHelper helper = new GoogleHelper("integration-tests");
		assertThat(helper).isNotNull();

		List<GoogleSpreadsheet> allSpreadsheets = helper.getAllSpreadsheets();
		assertThat(allSpreadsheets).hasSize(4);

		List<String> names = new ArrayList<>();
		for (GoogleSpreadsheet sheet : allSpreadsheets)
			names.add(sheet.getTitle());
		assertThat(names).containsOnly("WOL Series", "WOL Messages", "TBO Series", "TBO Messages");

	}

}
