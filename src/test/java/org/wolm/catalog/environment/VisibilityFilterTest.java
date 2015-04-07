package org.wolm.catalog.environment;

import static org.fest.assertions.Assertions.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.wolm.catalog.AccessLevel;
import org.wolm.message.Message;
import org.wolm.series.Series;

@RunWith(Enclosed.class)
public class VisibilityFilterTest {

	public static class SeriesTests {
		private VisibilityFilter publicFilterUnderTest;
		private VisibilityFilter protectedFilterUnderTest;
		private VisibilityFilter privateFilterUnderTest;

		private Series series;

		@Before
		public void beforeEachTest() {
			publicFilterUnderTest = new VisibilityFilter(AccessLevel.PUBLIC);
			protectedFilterUnderTest = new VisibilityFilter(AccessLevel.PROTECTED);
			privateFilterUnderTest = new VisibilityFilter(AccessLevel.PRIVATE);

			series = new Series();
		}

		@Test
		public void publicSeries() {
			series.setVisibility(AccessLevel.PUBLIC);
			assertThat(publicFilterUnderTest.shouldInclude(series)).isTrue();
			assertThat(protectedFilterUnderTest.shouldInclude(series)).isFalse();
			assertThat(privateFilterUnderTest.shouldInclude(series)).isFalse();
		}

		@Test
		public void protectedSeries() {
			series.setVisibility(AccessLevel.PROTECTED);
			assertThat(publicFilterUnderTest.shouldInclude(series)).isFalse();
			assertThat(protectedFilterUnderTest.shouldInclude(series)).isTrue();
			assertThat(privateFilterUnderTest.shouldInclude(series)).isFalse();
		}

		@Test
		public void privateSeries() {
			series.setVisibility(AccessLevel.PRIVATE);
			assertThat(publicFilterUnderTest.shouldInclude(series)).isFalse();
			assertThat(protectedFilterUnderTest.shouldInclude(series)).isFalse();
			assertThat(privateFilterUnderTest.shouldInclude(series)).isTrue();
		}

		@Test
		public void seriesWithoutVisibility() {
			assertThat(publicFilterUnderTest.shouldInclude(series)).isFalse();
			assertThat(protectedFilterUnderTest.shouldInclude(series)).isFalse();
			assertThat(privateFilterUnderTest.shouldInclude(series)).isTrue();
		}
	}

	public static class MessageTests {
		private VisibilityFilter publicFilterUnderTest;
		private VisibilityFilter protectedFilterUnderTest;
		private VisibilityFilter privateFilterUnderTest;

		private Message message;

		@Before
		public void beforeEachTest() {
			publicFilterUnderTest = new VisibilityFilter(AccessLevel.PUBLIC);
			protectedFilterUnderTest = new VisibilityFilter(AccessLevel.PROTECTED);
			privateFilterUnderTest = new VisibilityFilter(AccessLevel.PRIVATE);

			message = new Message();
		}

		@Test
		public void publicMessage() {
			message.setVisibility(AccessLevel.PUBLIC);
			assertThat(publicFilterUnderTest.shouldInclude(message)).isTrue();
			assertThat(protectedFilterUnderTest.shouldInclude(message)).isFalse();
			assertThat(privateFilterUnderTest.shouldInclude(message)).isFalse();
		}

		@Test
		public void protectedMessage() {
			message.setVisibility(AccessLevel.PROTECTED);
			assertThat(publicFilterUnderTest.shouldInclude(message)).isFalse();
			assertThat(protectedFilterUnderTest.shouldInclude(message)).isTrue();
			assertThat(privateFilterUnderTest.shouldInclude(message)).isFalse();
		}

		@Test
		public void privateMessage() {
			message.setVisibility(AccessLevel.PRIVATE);
			assertThat(publicFilterUnderTest.shouldInclude(message)).isFalse();
			assertThat(protectedFilterUnderTest.shouldInclude(message)).isFalse();
			assertThat(privateFilterUnderTest.shouldInclude(message)).isTrue();
		}
	}

	public static class MessageInSeriesTests {
		private VisibilityFilter publicFilterUnderTest;
		private VisibilityFilter protectedFilterUnderTest;
		private VisibilityFilter privateFilterUnderTest;

		private Series series;
		private Message message;

		@Before
		public void beforeEachTest() {
			publicFilterUnderTest = new VisibilityFilter(AccessLevel.PUBLIC);
			protectedFilterUnderTest = new VisibilityFilter(AccessLevel.PROTECTED);
			privateFilterUnderTest = new VisibilityFilter(AccessLevel.PRIVATE);

			series = new Series();
			message = new Message();
		}

		@Test
		public void publicMessageInSeries() {
			message.setVisibility(AccessLevel.PUBLIC);

			series.setVisibility(AccessLevel.PUBLIC);
			assertThat(publicFilterUnderTest.shouldInclude(series, message)).isTrue();
			assertThat(protectedFilterUnderTest.shouldInclude(series, message)).isFalse();
			assertThat(privateFilterUnderTest.shouldInclude(series, message)).isFalse();

			series.setVisibility(AccessLevel.PROTECTED);
			assertThat(publicFilterUnderTest.shouldInclude(series, message)).isFalse();
			assertThat(protectedFilterUnderTest.shouldInclude(series, message)).isTrue();
			assertThat(privateFilterUnderTest.shouldInclude(series, message)).isFalse();

			series.setVisibility(AccessLevel.PRIVATE);
			assertThat(publicFilterUnderTest.shouldInclude(series, message)).isFalse();
			assertThat(protectedFilterUnderTest.shouldInclude(series, message)).isFalse();
			assertThat(privateFilterUnderTest.shouldInclude(series, message)).isTrue();
		}

		@Test
		public void protectedMessageInSeries() {
			message.setVisibility(AccessLevel.PROTECTED);

			series.setVisibility(AccessLevel.PUBLIC);
			assertThat(publicFilterUnderTest.shouldInclude(series, message)).isFalse();
			assertThat(protectedFilterUnderTest.shouldInclude(series, message)).isFalse();
			assertThat(privateFilterUnderTest.shouldInclude(series, message)).isFalse();

			series.setVisibility(AccessLevel.PROTECTED);
			assertThat(publicFilterUnderTest.shouldInclude(series, message)).isFalse();
			assertThat(protectedFilterUnderTest.shouldInclude(series, message)).isTrue();
			assertThat(privateFilterUnderTest.shouldInclude(series, message)).isFalse();

			series.setVisibility(AccessLevel.PRIVATE);
			assertThat(publicFilterUnderTest.shouldInclude(series, message)).isFalse();
			assertThat(protectedFilterUnderTest.shouldInclude(series, message)).isFalse();
			assertThat(privateFilterUnderTest.shouldInclude(series, message)).isTrue();
		}

		@Test
		public void privateMessageInSeries() {
			message.setVisibility(AccessLevel.PRIVATE);

			series.setVisibility(AccessLevel.PUBLIC);
			assertThat(publicFilterUnderTest.shouldInclude(series, message)).isFalse();
			assertThat(protectedFilterUnderTest.shouldInclude(series, message)).isFalse();
			assertThat(privateFilterUnderTest.shouldInclude(series, message)).isFalse();

			series.setVisibility(AccessLevel.PROTECTED);
			assertThat(publicFilterUnderTest.shouldInclude(series, message)).isFalse();
			assertThat(protectedFilterUnderTest.shouldInclude(series, message)).isFalse();
			assertThat(privateFilterUnderTest.shouldInclude(series, message)).isFalse();

			series.setVisibility(AccessLevel.PRIVATE);
			assertThat(publicFilterUnderTest.shouldInclude(series, message)).isFalse();
			assertThat(protectedFilterUnderTest.shouldInclude(series, message)).isFalse();
			assertThat(privateFilterUnderTest.shouldInclude(series, message)).isTrue();
		}
	}
}
