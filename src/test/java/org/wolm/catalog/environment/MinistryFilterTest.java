package org.wolm.catalog.environment;

import static org.fest.assertions.Assertions.*;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.wolm.message.Message;
import org.wolm.series.Series;

@RunWith(Enclosed.class)
public class MinistryFilterTest {

	public static class SeriesInclusionTests {
		MinistryFilter filterUnderTest = new MinistryFilter().with("WOL");

		@Test
		public void seriesWithNoMessagesShouldNotBeIncluded() {
			Series series = new Series();
			assertThat(filterUnderTest.shouldInclude(series)).isFalse();
		}

		@Test
		public void seriesWithNoMessagesWithTheCorrectMinistryShouldNotBeIncluded() {
			Series series = new Series();

			Message m = new Message();
			m.setMinistry("CORE");
			series.addMessage(m);
			m = new Message();
			m.setMinistry("TBO");
			series.addMessage(m);

			assertThat(filterUnderTest.shouldInclude(series)).isFalse();
		}

		@Test
		public void seriesWithMessagesWithTheCorrectMinistryShouldBeIncluded() {
			Series series = new Series();

			Message m = new Message();
			m.setMinistry("WOL");
			series.addMessage(m);

			assertThat(filterUnderTest.shouldInclude(series)).isTrue();
		}

		@Test
		public void seriesWithMixedMessagesWithTheCorrectMinistryShouldBeIncluded() {
			Series series = new Series();

			Message m = new Message();
			m.setMinistry("WOL");
			series.addMessage(m);
			m = new Message();
			m.setMinistry("CORE");
			series.addMessage(m);

			assertThat(filterUnderTest.shouldInclude(series)).isTrue();
		}
	}

	public static class MessageInclusionTests {
		MinistryFilter filterUnderTest = new MinistryFilter().with("WOL");

		@Test
		public void seriesWithTheRightMinistryShouldBeIncluded() {
			Message message = new Message();
			message.setMinistry("WOL");
			assertThat(filterUnderTest.shouldInclude(message)).isTrue();
		}

		@Test
		public void seriesWithTheWrongMinistryShouldNotBeIncluded() {
			Message message = new Message();
			message.setMinistry("TBO");
			assertThat(filterUnderTest.shouldInclude(message)).isFalse();
		}

		@Test
		public void seriesWithNoMinistryShouldNotBeIncluded() {
			Message message = new Message();
			assertThat(filterUnderTest.shouldInclude(message)).isFalse();
		}
	}

	public static class SeriesExclusionTests {
		MinistryFilter filterUnderTest = new MinistryFilter().without("CORE", "Ask Pastor");

		@Test
		public void seriesWithNoMessagesShouldNotBeIncluded() {
			Series series = new Series();
			assertThat(filterUnderTest.shouldInclude(series)).isFalse();
		}

		@Test
		public void seriesWithNoMessagesWithTheIncorrectMinistryShouldNotBeIncluded() {
			Series series = new Series();

			Message m = new Message();
			m.setMinistry("CORE");
			series.addMessage(m);

			assertThat(filterUnderTest.shouldInclude(series)).isFalse();
		}

		@Test
		public void seriesWithMixedMessagesWithTheIncorrectMinistryShouldNotBeIncluded() {
			Series series = new Series();

			Message m = new Message();
			m.setMinistry("CORE");
			series.addMessage(m);
			m = new Message();
			m.setMinistry("Ask Pastor");
			series.addMessage(m);

			assertThat(filterUnderTest.shouldInclude(series)).isFalse();
		}

		@Test
		public void seriesWithMessagesWithTheCorrectMinistryShouldBeIncluded() {
			Series series = new Series();

			Message m = new Message();
			m.setMinistry("WOL");
			series.addMessage(m);

			assertThat(filterUnderTest.shouldInclude(series)).isTrue();
		}

		@Test
		public void seriesWithMixedMessagesWithTheCorrectMinistryShouldBeIncluded() {
			Series series = new Series();

			Message m = new Message();
			m.setMinistry("WOL");
			series.addMessage(m);
			m = new Message();
			m.setMinistry("TBO");
			series.addMessage(m);

			assertThat(filterUnderTest.shouldInclude(series)).isTrue();
		}

		@Test
		public void seriesWithMixedMessagesWithAnyCorrectMinistryShouldBeIncluded() {
			Series series = new Series();

			Message m = new Message();
			m.setMinistry("TBO");
			series.addMessage(m);
			m = new Message();
			m.setMinistry("Ask Pastor");
			series.addMessage(m);

			assertThat(filterUnderTest.shouldInclude(series)).isTrue();
		}
	}

	public static class MessageExclusionTests {
		MinistryFilter filterUnderTest = new MinistryFilter().without("CORE");

		@Test
		public void seriesWithTheRightMinistryShouldBeIncluded() {
			Message message = new Message();
			message.setMinistry("WOL");
			assertThat(filterUnderTest.shouldInclude(message)).isTrue();
		}

		@Test
		public void seriesWithTheWrongMinistryShouldNotBeIncluded() {
			Message message = new Message();
			message.setMinistry("CORE");
			assertThat(filterUnderTest.shouldInclude(message)).isFalse();
		}

		@Test
		public void seriesWithNoMinistryShouldNotBeIncluded() {
			Message message = new Message();
			assertThat(filterUnderTest.shouldInclude(message)).isFalse();
		}
	}
}
