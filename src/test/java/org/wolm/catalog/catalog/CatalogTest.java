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
		public void bookletsShouldBeIncluded() {
			Series booklet = createBookletSeries();
			catalogUnderTest.add(booklet);

			List<NamedLink> resources = catalogUnderTest.getResources();

			assertThat(resources).containsOnly(booklet.getResources(false).get(0));
		}

		@Test
		public void privateBookletsShouldNotBeIncluded() {
			Series booklet = createBookletSeries();
			catalogUnderTest.add(booklet);

			// check private
			booklet.setVisibility(AccessLevel.PRIVATE);
			List<NamedLink> resources = catalogUnderTest.getResources();
			assertThat(resources).isEmpty();

			// check protected
			booklet.setVisibility(AccessLevel.PROTECTED);
			resources = catalogUnderTest.getResources();
			assertThat(resources).isEmpty();
		}

		@Test
		public void seriesWithBookletShouldBeIncluded() {
			Series series = createSeriesWithBooklet();
			catalogUnderTest.add(series);

			List<NamedLink> resources = catalogUnderTest.getResources();

			assertThat(resources).containsOnly(series.getResources(false).get(0));
		}

		@Test
		public void privateSeriesWithBookletShouldNotBeIncluded() {
			Series series = createSeriesWithBooklet();
			catalogUnderTest.add(series);

			// check private
			series.setVisibility(AccessLevel.PRIVATE);
			List<NamedLink> resources = catalogUnderTest.getResources();
			assertThat(resources).isEmpty();

			// check protected
			series.setVisibility(AccessLevel.PROTECTED);
			resources = catalogUnderTest.getResources();
			assertThat(resources).isEmpty();
		}

		@Test
		public void seriesMessageBookletShouldBeIncluded() {
			Series series = createSeriesAndMessageWithBooklet();
			catalogUnderTest.add(series);

			List<NamedLink> resources = catalogUnderTest.getResources();

			NamedLink seriesBooklet = series.getResources(false).get(0);
			NamedLink messageBooklet = series.getMessages().get(0).getResources().get(0);
			assertThat(resources).containsOnly(seriesBooklet, messageBooklet);
		}

		@Test
		public void publicSeriesPrivateMessageBookletShouldNotBeIncluded() {
			Series series = createSeriesAndMessageWithBooklet();
			catalogUnderTest.add(series);

			series.getMessages().get(0).setVisibility(AccessLevel.PRIVATE);
			List<NamedLink> resources = catalogUnderTest.getResources();

			// the series is public and it's booklet should be included, but the message is private so it's book
			// shouldn't be included
			NamedLink seriesBooklet = series.getResources(false).get(0);
			assertThat(resources).containsOnly(seriesBooklet);
		}

		@Test
		public void standAloneMessageBookletShouldBeIncluded() {
			Series series = createSeriesAndMessageWithBooklet();
			catalogUnderTest.add(series);

			Message message = createMessageWithBooklet();
			catalogUnderTest.add(message);

			List<NamedLink> resources = catalogUnderTest.getResources();

			NamedLink seriesBooklet = series.getResources(false).get(0);
			NamedLink seriesMessageBooklet = series.getMessages().get(0).getResources().get(0);
			NamedLink messageBooklet = message.getResources().get(0);
			assertThat(resources).containsOnly(seriesBooklet, seriesMessageBooklet, messageBooklet);
		}

		private Series createBookletSeries() {
			Series booklet = new Series();
			booklet.setTitle("BOOKLET");
			booklet.setResourcesAsString("http://org.wolm.com/BOOKLET.PDF");
			booklet.setVisibility(AccessLevel.PUBLIC);
			return booklet;
		}

		private Series createSeriesWithBooklet() {
			Series series = new Series();
			series.setTitle("SERIES WITH BOOKLET");
			series.setMessageCount(1L);
			series.setStartDate(new Date());
			series.setResourcesAsString("http://org.wolm.com/BOOKLET.PDF");
			series.setVisibility(AccessLevel.PUBLIC);
			return series;
		}

		private Message createMessageWithBooklet() {
			Message message = new Message();
			message.setTitle("MESSAGE WITH BOOKLET");
			message.setVisibility(AccessLevel.PUBLIC);
			message.setResourcesAsString("http://org.wolm.com/MSG-BOOKLET.PDF");

			return message;
		}

		private Series createSeriesAndMessageWithBooklet() {
			Series series = new Series();
			series.setTitle("SERIES WITH MESSAGE WITH BOOKLET");
			series.setMessageCount(1L);
			series.setStartDate(new Date());
			series.setResourcesAsString("http://org.wolm.com/SERIES-BOOKLET.PDF");
			series.setVisibility(AccessLevel.PUBLIC);

			Message message = createMessageWithBooklet();
			message.setResourcesAsString("http://org.wolm.com/SERIES-MSG-BOOKLET.PDF");
			series.addMessage(message);

			return series;
		}
	}

}
