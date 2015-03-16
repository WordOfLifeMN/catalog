package org.wolm.catalog.catalog;

import static org.fest.assertions.Assertions.*;

import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.wolm.catalog.AccessLevel;
import org.wolm.catalog.NamedLink;
import org.wolm.catalog.RenderFactory;
import org.wolm.message.Message;
import org.wolm.series.Series;

@RunWith(Enclosed.class)
public class CatalogTest {

	public static class TestSeries {
		Catalog catalogUnderTest;

		@Before
		public void beforeEachTest() {
			catalogUnderTest = new Catalog();
			RenderFactory.setMinVisibility(AccessLevel.PUBLIC);
		}

		@After
		public void afterEachTest() {
			RenderFactory.setMinVisibility(null);
		}

		@Test
		public void publicSeriesShouldBeIncludedForPublicList() {
			Series series = createSeries();
			catalogUnderTest.add(series);

			assertThat(catalogUnderTest.getSeries()).containsOnly(series);
		}

		@Test
		public void privateSeriesShouldNotBeIncludedForPublicList() {
			Series series = createSeries();
			catalogUnderTest.add(series);

			series.setVisibility(AccessLevel.PRIVATE);
			assertThat(catalogUnderTest.getSeries()).isEmpty();

			series.setVisibility(AccessLevel.PROTECTED);
			assertThat(catalogUnderTest.getSeries()).isEmpty();
		}

		private Series createSeries() {
			Series series = new Series();
			series.setTitle("SERIES");
			series.setVisibility(AccessLevel.PUBLIC);
			return series;
		}

	}

	public static class TestResources {
		private Catalog catalogUnderTest;

		@Before
		public void beforeEachTest() {
			catalogUnderTest = new Catalog();
			RenderFactory.setMinVisibility(AccessLevel.PUBLIC);
		}

		@After
		public void afterEachTest() {
			RenderFactory.setMinVisibility(null);
		}

		@Test
		public void bookletListShouldIncludeSeriesBooklets() {
			Series series = createBookletSeries();
			catalogUnderTest.add(series);

			List<NamedLink> resources = catalogUnderTest.getBooklets();

			assertThat(resources).containsOnly(series.getBooklets().get(0));
		}

		@Test
		public void handoutsShouldNotIncludeSeriesBooklets() {
			Series booklet = createBookletSeries();
			catalogUnderTest.add(booklet);

			List<NamedLink> resources = catalogUnderTest.getHandoutsAndResources();

			assertThat(resources).isEmpty();
		}

		@Test
		public void bookletListShouldIncludeSeriesWithBooklet() {
			Series series = createSeriesWithBooklet();
			catalogUnderTest.add(series);

			List<NamedLink> booklets = catalogUnderTest.getBooklets();

			assertThat(booklets).containsOnly(series.getBooklets().get(0));
		}

		@Test
		public void privateSeriesWithBookletShouldNotBeIncluded() {
			Series series = createSeriesWithBooklet();
			catalogUnderTest.add(series);

			// check private
			series.setVisibility(AccessLevel.PRIVATE);
			List<NamedLink> resources = catalogUnderTest.getHandoutsAndResources();
			assertThat(resources).isEmpty();

			// check protected
			series.setVisibility(AccessLevel.PROTECTED);
			resources = catalogUnderTest.getHandoutsAndResources();
			assertThat(resources).isEmpty();
		}

		@Test
		public void handoutResourcesShouldNotIncludeBooklets() {
			Series series = createSeriesWithResourceAndMessageWithResource();
			catalogUnderTest.add(series);

			List<NamedLink> resources = catalogUnderTest.getHandoutsAndResources();

			NamedLink seriesResource = series.getResources().get(0);
			NamedLink messageResource = series.getMessages().get(0).getResources().get(0);
			assertThat(resources).containsOnly(seriesResource, messageResource);
		}

		@Test
		public void standAloneMessageBookletShouldBeIncluded() {
			Series series = createSeriesWithResourceAndMessageWithResource();
			catalogUnderTest.add(series);

			Message message = createMessageWithResource();
			catalogUnderTest.add(message);

			List<NamedLink> resources = catalogUnderTest.getHandoutsAndResources();

			NamedLink seriesResource = series.getResources().get(0);
			NamedLink seriesMessageResource = series.getMessages().get(0).getResources().get(0);
			NamedLink messageResource = message.getResources().get(0);
			assertThat(resources).containsOnly(seriesResource, seriesMessageResource, messageResource);
		}

		@Test
		public void youtubeLinksShouldBeIncluded() {
			Message message = createMessageWithResource();
			message.setResourcesAsString("https://s3-us-west-2.amazonaws.com/wordoflife.mn.BUCKET/MSG-BOOKLET.PDF;http://youtu.be/blahblah");
			catalogUnderTest.add(message);

			List<NamedLink> resources = catalogUnderTest.getHandoutsAndResources();

			assertThat(resources).containsOnly(message.getResources().get(0), message.getResources().get(1));
		}

		@Test
		public void bookletsShouldContainBookletsFromPrivateSeries() {
			Series series = createSeriesWithBooklet();
			series.setVisibility(AccessLevel.PRIVATE);
			catalogUnderTest.add(series);

			List<NamedLink> booklets = catalogUnderTest.getBooklets();

			// the booklet may be present, but it will not be the same object, so built a NamedLink to test agains
			assertThat(booklets).contains(new NamedLink(series.getBooklets().get(0)));
		}

		/** Creates a booklet-series. That is, a series that only contains a booklet and no messages */
		private Series createBookletSeries() {
			Series booklet = new Series();
			booklet.setTitle("BOOKLET");
			booklet.setBookletsAsString("https://s3-us-west-2.amazonaws.com/wordoflife.mn.BUCKET/BOOKLET.PDF");
			booklet.setVisibility(AccessLevel.PUBLIC);
			return booklet;
		}

		/** Creates a series with a booklet. That is, a series that has a booklet and messages */
		private Series createSeriesWithBooklet() {
			Series series = new Series();
			series.setTitle("SERIES WITH BOOKLET");
			series.setMessageCount(1L);
			series.setStartDate(new Date());
			series.setBookletsAsString("https://s3-us-west-2.amazonaws.com/wordoflife.mn.BUCKET/SERIES-BOOKLET.PDF");
			series.setVisibility(AccessLevel.PUBLIC);
			return series;
		}

		private Message createMessageWithResource() {
			Message message = new Message();
			message.setTitle("MESSAGE WITH BOOKLET");
			message.setVisibility(AccessLevel.PUBLIC);
			message.setResourcesAsString("https://s3-us-west-2.amazonaws.com/wordoflife.mn.BUCKET/MSG-RESOURCE.PDF");

			return message;
		}

		private Series createSeriesWithResourceAndMessageWithResource() {
			Series series = new Series();
			series.setTitle("SERIES WITH MESSAGE WITH BOOKLET");
			series.setMessageCount(1L);
			series.setStartDate(new Date());
			series.setResourcesAsString("https://s3-us-west-2.amazonaws.com/wordoflife.mn.BUCKET/SERIES-RESOURCE.PDF");
			series.setVisibility(AccessLevel.PUBLIC);

			Message message = createMessageWithResource();
			message.setResourcesAsString("https://s3-us-west-2.amazonaws.com/wordoflife.mn.BUCKET/SERIES-MSG-RESOURCE.PDF");
			series.addMessage(message);

			return series;
		}
	}

}
