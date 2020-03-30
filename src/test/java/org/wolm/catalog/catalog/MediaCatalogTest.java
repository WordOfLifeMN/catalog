package org.wolm.catalog.catalog;

import static org.fest.assertions.Assertions.*;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.wolm.catalog.AccessLevel;
import org.wolm.catalog.NamedLink;
import org.wolm.catalog.environment.RenderEnvironment;
import org.wolm.catalog.environment.VisibilityFilter;
import org.wolm.message.Message;
import org.wolm.series.Series;

@RunWith(Enclosed.class)
public class MediaCatalogTest {

	public static class TestMessage {
		MediaCatalog sut;

		@Before
		public void beforeEachTest() {
			sut = new MediaCatalog();
		}

		@After
		public void afterEachTest() {
			RenderEnvironment.instance().clearFilters();
		}

		@Test
		public void messageInSeriesIsNotStandAlone() {
			Message msg = createMessage();
			msg.setSeries(Arrays.asList(new String[] { "SERIES" }));
			sut.add(msg);

			assertThat(sut.getStandAloneMessagesInSeriesByMessage()).isEmpty();
		}

		@Test
		public void messageWithoutSeriesIsStandAlone() {
			Message msg = createMessage();
			sut.add(msg);

			assertThat(sut.getStandAloneMessagesInSeriesByMessage()).hasSize(1);
		}

		@Test
		public void messageInStandAloneSeriesIsStandAlone() {
			Message msg = createMessage();
			sut.add(msg);

			msg.setSeries(Arrays.asList(new String[] { "SAM" }));
			assertThat(sut.getStandAloneMessagesInSeriesByMessage()).hasSize(1);

			msg.setSeries(Arrays.asList(new String[] { "stand alone" }));
			assertThat(sut.getStandAloneMessagesInSeriesByMessage()).hasSize(1);

			msg.setSeries(Arrays.asList(new String[] { "stand alone MESSAGE" }));
			assertThat(sut.getStandAloneMessagesInSeriesByMessage()).hasSize(1);
		}

		@Test
		public void messageInStandAloneAndOtherSeriesIsStandAlone() {
			Message msg = createMessage();
			sut.add(msg);

			msg.setSeries(Arrays.asList(new String[] { "sam", "SERIES" }));
			assertThat(sut.getStandAloneMessagesInSeriesByMessage()).hasSize(1);
		}

		private Message createMessage() {
			Message msg = new Message();
			msg.setTitle("MESSAGE");
			msg.setVisibility(AccessLevel.PUBLIC);
			msg.setDate(new Date(2020, 0, 1, 10, 0));
			return msg;
		}
	}

	public static class TestSeries {
		MediaCatalog sut;

		@Before
		public void beforeEachTest() {
			sut = new MediaCatalog();
		}

		@After
		public void afterEachTest() {
			RenderEnvironment.instance().clearFilters();
		}

		@Test
		public void publicSeriesShouldBeIncludedForPublicList() {
			Series series = createSeries();
			sut.add(series);

			RenderEnvironment.instance().addFilter(new VisibilityFilter(AccessLevel.PUBLIC));

			assertThat(sut.getFilteredSeries()).containsOnly(series);
		}

		@Test
		public void privateSeriesShouldNotBeIncludedForPublicList() {
			Series series = createSeries();
			sut.add(series);

			RenderEnvironment.instance().addFilter(new VisibilityFilter(AccessLevel.PUBLIC));

			series.setVisibility(AccessLevel.PRIVATE);
			assertThat(sut.getFilteredSeries()).isEmpty();

			series.setVisibility(AccessLevel.PROTECTED);
			assertThat(sut.getFilteredSeries()).isEmpty();
		}

		@Test
		public void seriesCanBeFound() {
			Series series = createSeries();
			sut.add(series);

			assertThat(sut.getFilteredSeries()).hasSize(1);
			assertThat(sut.getFilteredSeries().get(0).getTitle()).isEqualTo("SERIES");
		}

		private Series createSeries() {
			Series series = new Series();
			series.setTitle("SERIES");
			series.setVisibility(AccessLevel.PUBLIC);
			return series;
		}

	}

	public static class TestResources {
		private MediaCatalog sut;

		@Before
		public void beforeEachTest() {
			sut = new MediaCatalog();
			RenderEnvironment.instance().addFilter(new VisibilityFilter(AccessLevel.PUBLIC));
		}

		@After
		public void afterEachTest() {
			RenderEnvironment.instance().clearFilters();
		}

		@Test
		public void bookletListShouldIncludeSeriesBooklets() {
			Series series = createBookletSeries();
			sut.add(series);

			List<NamedLink> resources = sut.getBooklets();

			assertThat(resources).containsOnly(series.getBooklets().get(0));
		}

		@Test
		public void handoutsShouldNotIncludeSeriesBooklets() {
			Series booklet = createBookletSeries();
			sut.add(booklet);

			List<NamedLink> resources = sut.getHandoutsAndResources();

			assertThat(resources).isEmpty();
		}

		@Test
		public void bookletListShouldIncludeSeriesWithBooklet() {
			Series series = createSeriesWithBooklet();
			sut.add(series);

			List<NamedLink> booklets = sut.getBooklets();

			assertThat(booklets).containsOnly(series.getBooklets().get(0));
		}

		@Test
		public void privateSeriesWithBookletShouldNotBeIncluded() {
			Series series = createSeriesWithBooklet();
			sut.add(series);

			// check private
			series.setVisibility(AccessLevel.PRIVATE);
			List<NamedLink> resources = sut.getHandoutsAndResources();
			assertThat(resources).isEmpty();

			// check protected
			series.setVisibility(AccessLevel.PROTECTED);
			resources = sut.getHandoutsAndResources();
			assertThat(resources).isEmpty();
		}

		@Test
		public void handoutResourcesShouldNotIncludeBooklets() {
			Series series = createSeriesWithResourceAndMessageWithResource();
			sut.add(series);

			List<NamedLink> resources = sut.getHandoutsAndResources();

			NamedLink seriesResource = series.getResources().get(0);
			NamedLink messageResource = series.getMessages().get(0).getResources().get(0);
			assertThat(resources).containsOnly(seriesResource, messageResource);
		}

		@Test
		public void standAloneMessageBookletShouldBeIncluded() {
			Series series = createSeriesWithResourceAndMessageWithResource();
			sut.add(series);

			Message message = createMessageWithResource();
			sut.add(message);

			List<NamedLink> resources = sut.getHandoutsAndResources();

			NamedLink seriesResource = series.getResources().get(0);
			NamedLink seriesMessageResource = series.getMessages().get(0).getResources().get(0);
			NamedLink messageResource = message.getResources().get(0);
			assertThat(resources).containsOnly(seriesResource, seriesMessageResource, messageResource);
		}

		@Test
		public void youtubeLinksShouldBeIncluded() {
			Message message = createMessageWithResource();
			message.setResourcesAsString(
					"https://s3-us-west-2.amazonaws.com/wordoflife.mn.BUCKET/MSG-BOOKLET.PDF;http://youtu.be/blahblah");
			sut.add(message);

			List<NamedLink> resources = sut.getHandoutsAndResources();

			assertThat(resources).containsOnly(message.getResources().get(0), message.getResources().get(1));
		}

		@Test
		public void bookletsShouldContainBookletsFromPrivateSeries() {
			Series series = createSeriesWithBooklet();
			series.setVisibility(AccessLevel.PRIVATE);
			sut.add(series);

			List<NamedLink> booklets = sut.getBooklets();

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
			message.setResourcesAsString(
					"https://s3-us-west-2.amazonaws.com/wordoflife.mn.BUCKET/SERIES-MSG-RESOURCE.PDF");
			series.addMessage(message);

			return series;
		}
	}

}
