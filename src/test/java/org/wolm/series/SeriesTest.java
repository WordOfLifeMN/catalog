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
			bookletUnderTest.setBookletsAsString("http://org.wolm.com/none.jpg");
		}

		@Test
		public void bookletOnlyShouldBeValidBooklet() {
			assertThat(bookletUnderTest.isBooklet()).isTrue();
			assertThat(bookletUnderTest.isValid(System.out, null)).isTrue();
		}

		@Test
		public void resourceWithBookletOnlyShouldBeValidBooklet() {
			bookletUnderTest.setResourcesAsString("http://org.wolm.com/none.jpg");
			assertThat(bookletUnderTest.isBooklet()).isTrue();
			assertThat(bookletUnderTest.isValid(System.out, null)).isTrue();
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
		public void withoutBookletShouldNotBeBooklet() {
			bookletUnderTest.setBookletsAsString(null);
			assertThat(bookletUnderTest.isBooklet()).isFalse();
		}
	}
}
