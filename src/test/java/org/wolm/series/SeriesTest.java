package org.wolm.series;

import static org.fest.assertions.Assertions.*;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class SeriesTest {

	public static class Booklets {
		private Series bookletUnderTest;

		@Before
		public void beforeEachTest() {
			bookletUnderTest = new Series();
			bookletUnderTest.setTitle("UNIT TEST BOOKLET");
			bookletUnderTest.setResourcesAsString("http://org.wolm.com/none.jpg");
		}

		@Test
		public void resourceOnlyShouldBeValidBooklet() {
			assertThat(bookletUnderTest.isBooklet()).isTrue();
			assertThat(bookletUnderTest.isValid(System.out)).isTrue();
		}

		@Test
		public void withStartDateShouldNotBeBooklet() {
			bookletUnderTest.setStartDate(new Date());
			assertThat(bookletUnderTest.isBooklet()).isFalse();
		}

		@Test
		public void withMessagesShouldNotBeBooklet() {
			bookletUnderTest.setMessageCount(1L);
			assertThat(bookletUnderTest.isBooklet()).isFalse();
		}

		@Test
		public void withoutResourceShouldNotBeBooklet() {
			bookletUnderTest.setResourcesAsString(null);
			assertThat(bookletUnderTest.isBooklet()).isFalse();
		}
	}
}
