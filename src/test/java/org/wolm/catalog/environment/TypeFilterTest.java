package org.wolm.catalog.environment;

import static org.fest.assertions.Assertions.*;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.wolm.message.Message;
import org.wolm.series.Series;

@RunWith(Enclosed.class)
public class TypeFilterTest {

	public static class SeriesInclusionTests {
		TypeFilter filterUnderTest = new TypeFilter().with("Message");

		@Test
		public void seriesWithNoMessagesShouldNotBeIncluded() {
			Series series = new Series();
			assertThat(filterUnderTest.shouldInclude(series)).isFalse();
		}

		@Test
		public void seriesWithNoMessagesWithTheCorrectTypeShouldNotBeIncluded() {
			Series series = new Series();

			Message m = new Message();
			m.setType("Song");
			series.addMessage(m);
			m = new Message();
			m.setType("C.O.R.E.");
			series.addMessage(m);

			assertThat(filterUnderTest.shouldInclude(series)).isFalse();
		}

		@Test
		public void seriesWithMessagesWithTheCorrectTypeShouldBeIncluded() {
			Series series = new Series();

			Message m = new Message();
			m.setType("Message");
			series.addMessage(m);

			assertThat(filterUnderTest.shouldInclude(series)).isTrue();
		}

		@Test
		public void seriesWithMixedMessagesWithTheCorrectTypeShouldBeIncluded() {
			Series series = new Series();

			Message m = new Message();
			m.setType("Message");
			series.addMessage(m);
			m = new Message();
			m.setType("C.O.R.E.");
			series.addMessage(m);

			assertThat(filterUnderTest.shouldInclude(series)).isTrue();
		}
	}

	public static class MessageInclusionTests {
		TypeFilter filterUnderTest = new TypeFilter().with("Message");

		@Test
		public void seriesWithTheRightTypeShouldBeIncluded() {
			Message message = new Message();
			message.setType("Message");
			assertThat(filterUnderTest.shouldInclude(message)).isTrue();
		}

		@Test
		public void seriesWithTheWrongTypeShouldNotBeIncluded() {
			Message message = new Message();
			message.setType("Song");
			assertThat(filterUnderTest.shouldInclude(message)).isFalse();
		}

		@Test
		public void seriesWithNoTypeShouldNotBeIncluded() {
			Message message = new Message();
			assertThat(filterUnderTest.shouldInclude(message)).isFalse();
		}
	}

	public static class SeriesExclusionTests {
		TypeFilter filterUnderTest = new TypeFilter().without("Song", "Testimony");

		@Test
		public void seriesWithNoMessagesShouldNotBeIncluded() {
			Series series = new Series();
			assertThat(filterUnderTest.shouldInclude(series)).isFalse();
		}

		@Test
		public void seriesWithNoMessagesWithTheIncorrectTypeShouldNotBeIncluded() {
			Series series = new Series();

			Message m = new Message();
			m.setType("Song");
			series.addMessage(m);

			assertThat(filterUnderTest.shouldInclude(series)).isFalse();
		}

		@Test
		public void seriesWithMixedMessagesWithTheIncorrectTypeShouldNotBeIncluded() {
			Series series = new Series();

			Message m = new Message();
			m.setType("Song");
			series.addMessage(m);
			m = new Message();
			m.setType("Testimony");
			series.addMessage(m);

			assertThat(filterUnderTest.shouldInclude(series)).isFalse();
		}

		@Test
		public void seriesWithMessagesWithTheCorrectTypeShouldBeIncluded() {
			Series series = new Series();

			Message m = new Message();
			m.setType("Message");
			series.addMessage(m);

			assertThat(filterUnderTest.shouldInclude(series)).isTrue();
		}

		@Test
		public void seriesWithMixedMessagesWithTheCorrectTypeShouldBeIncluded() {
			Series series = new Series();

			Message m = new Message();
			m.setType("Message");
			series.addMessage(m);
			m = new Message();
			m.setType("C.O.R.E.");
			series.addMessage(m);

			assertThat(filterUnderTest.shouldInclude(series)).isTrue();
		}

		@Test
		public void seriesWithMixedMessagesWithAnyCorrectTypeShouldBeIncluded() {
			Series series = new Series();

			Message m = new Message();
			m.setType("Song");
			series.addMessage(m);
			m = new Message();
			m.setType("C.O.R.E.");
			series.addMessage(m);

			assertThat(filterUnderTest.shouldInclude(series)).isTrue();
		}
	}

	public static class MessageExclusionTests {
		TypeFilter filterUnderTest = new TypeFilter().without("Song");

		@Test
		public void seriesWithTheRightTypeShouldBeIncluded() {
			Message message = new Message();
			message.setType("Message");
			assertThat(filterUnderTest.shouldInclude(message)).isTrue();
		}

		@Test
		public void seriesWithTheWrongTypeShouldNotBeIncluded() {
			Message message = new Message();
			message.setType("Song");
			assertThat(filterUnderTest.shouldInclude(message)).isFalse();
		}

		@Test
		public void seriesWithNoTypeShouldNotBeIncluded() {
			Message message = new Message();
			assertThat(filterUnderTest.shouldInclude(message)).isFalse();
		}
	}
}
