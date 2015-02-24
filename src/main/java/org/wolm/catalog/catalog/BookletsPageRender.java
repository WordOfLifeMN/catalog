package org.wolm.catalog.catalog;

import java.util.List;

import org.wolm.catalog.NamedLink;

public class BookletsPageRender extends ResourcesPageRender {

	public BookletsPageRender(List<NamedLink> resources) {
		super("booklets", resources);
	}

}
