package org.wolm.catalog.catalog;

import java.util.List;

import org.wolm.series.Series;

/**
 * Renders a page containing multiple series (i.e. an index of series). The data model will include the name of the
 * promo that should be included on the page.
 * <p>
 * The promo name must be a file-safe name like 'herkimer'. Internally, this will be stored in the page as a promotional
 * file name like 'promo-herkimer.ftl', which must exist in the Freemarker templates folder
 * 
 * @author wolm
 */
public class SeriesIndexWithPromoPageRender extends SeriesIndexPageRender {
	private String promoName;

	public SeriesIndexWithPromoPageRender(List<Series> seriesList) {
		super(seriesList);
	}

	public String getPromoName() {
		return promoName;
	}

	public void setPromoName(String promoName) {
		this.promoName = promoName;
		addDataToModel("promoFileName", "promo-" + promoName + ".ftl");
	}

	public void setPromoSeries(List<Series> promoSeries) {
		addDataToModel("promoSeriesList", promoSeries);
	}
}
