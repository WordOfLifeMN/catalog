package org.wolm.catalog;

import static org.fest.assertions.Assertions.*;

import org.junit.Test;
import org.wolm.catalog.catalog.Catalog;

public class CatalogIT {

	@Test
	public void shouldBeAbleToReadCatalog() throws Exception {
		Catalog catalog = new Catalog();
		catalog.setMessageSpreadsheetName("zITMessages");
		catalog.init();

		assertThat(catalog.getMessages().size()).isEqualTo(3);
	}

}
