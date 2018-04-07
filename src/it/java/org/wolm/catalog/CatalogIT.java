package org.wolm.catalog;

import static org.fest.assertions.Assertions.*;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.wolm.catalog.catalog.MediaCatalog;

@RunWith(Enclosed.class)
public class CatalogIT {

	public static class WolCatalog {
		@Test
		public void defaultCatalog() throws Exception {
			MediaCatalog catalog = new MediaCatalog();
			catalog.populateFromGoogleSpreadsheets();

			assertThat(catalog.getSeries().size()).isGreaterThan(100);
			assertThat(catalog.getMessages().size()).isGreaterThan(1000);
		}

		@Test
		public void explicitCatalog() throws Exception {
			MediaCatalog catalog = new MediaCatalog("WOL Series", "WOL Messages");
			catalog.populateFromGoogleSpreadsheets();

			assertThat(catalog.getSeries().size()).isGreaterThan(100);
			assertThat(catalog.getMessages().size()).isGreaterThan(1000);
		}
	}

	public static class TboCatalog {

		@Test
		public void explicitCatalog() throws Exception {
			MediaCatalog catalog = new MediaCatalog("TBO Series", "TBO Messages");
			catalog.populateFromGoogleSpreadsheets();

			assertThat(catalog.getSeries().size()).isGreaterThan(100);
			assertThat(catalog.getMessages().size()).isGreaterThan(1000);
		}
	}
}
