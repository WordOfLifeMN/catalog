package org.wolm.catalog;

import static org.fest.assertions.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.wolm.message.Message;

@RunWith(Enclosed.class)
public class MessageTest {

	public static class Validation {
		private Message messageUnderTest = null;
		private ByteArrayOutputStream validationMessage = null;
		private PrintStream outStream = null;

		@Before
		public void beforeEachTest() throws Exception {
			messageUnderTest = new Message();
			messageUnderTest.setTitle("UNIT TEST");
			messageUnderTest.setDate(new Date());
			messageUnderTest.setSeries(Arrays.asList(new String[] { "ONE", "TWO" }));
			messageUnderTest.setTrackNumbers(Arrays.asList(new Integer[] { 1, 2 }));
			messageUnderTest.setMinistry("WOL");
			messageUnderTest.setType("Message");
			messageUnderTest.setVisibilityAsString("Private");
			messageUnderTest.setAudioLink(new URL("http://audio.com/link.wav"));
			messageUnderTest.setVideoLink(new URL("http://video.com/link.mov"));

			validationMessage = new ByteArrayOutputStream();
			outStream = new PrintStream(validationMessage);
		}

		@After
		public void afterEachTest() {
			messageUnderTest = null;
			outStream.close();
			validationMessage = null;
			outStream = null;
		}

		@Test
		public void shouldBeValid() {
			assertThat(messageUnderTest.isValid(outStream)).isTrue();
			assertThat(validationMessage.toString()).isEmpty();
		}

		@Test
		public void shouldHaveTitle() {
			messageUnderTest.setTitle(null);
			assertThat(messageUnderTest.isValid(outStream)).isFalse();
			assertThat(validationMessage.toString()).contains("has no title");
		}

		@Test
		public void shouldHaveDate() {
			messageUnderTest.setDate(null);
			assertThat(messageUnderTest.isValid(outStream)).isFalse();
			assertThat(validationMessage.toString()).contains("has no date");
		}

		@Test
		public void ministryIsMissing() {
			messageUnderTest.setMinistry(null);
			assertThat(messageUnderTest.isValid(outStream)).isFalse();
			assertThat(validationMessage.toString()).contains("has no ministry");
		}

		@Test
		public void ministryIsInvalid() {
			messageUnderTest.setMinistry("TESTING");
			assertThat(messageUnderTest.isValid(outStream)).isFalse();
			assertThat(validationMessage.toString()).contains("unrecognized ministry");
		}

		@Test
		public void shouldHaveTracksToMatchSeries() {
			messageUnderTest.setSeries(null);
			messageUnderTest.setTrackNumbers(Arrays.asList(new Integer[] { 1 }));
			assertThat(messageUnderTest.isValid(outStream)).isFalse();
			assertThat(validationMessage.toString()).contains("0 series, but has track data for 1");

			messageUnderTest.setSeries(Arrays.asList(new String[] { "ONE", "TWO" }));
			messageUnderTest.setTrackNumbers(Arrays.asList(new Integer[] { 1 }));
			assertThat(messageUnderTest.isValid(outStream)).isFalse();
			assertThat(validationMessage.toString()).contains("2 series, but has track data for 1");
		}

		@Test
		public void hyphenSeriesShouldValidateWithoutTrack() {
			messageUnderTest.setSeries(Arrays.asList(new String[] { "-" }));
			messageUnderTest.setTrackNumbers(null);
			assertThat(messageUnderTest.isValid(outStream)).isTrue();
		}

		@Test
		public void shouldHaveValidType() {
			messageUnderTest.setType("TESTING");
			assertThat(messageUnderTest.isValid(outStream)).isTrue(); // bad type is a warning, not an error
			assertThat(validationMessage.toString()).contains("unknown type");
		}

		@Test
		public void shouldHaveValidVisibility() {
			messageUnderTest.setVisibilityAsString("VISIBLE");
			assertThat(messageUnderTest.isValid(outStream)).isFalse();
			assertThat(validationMessage.toString()).contains("unknown visibility");
		}

		@Test
		public void shouldHaveValidAudioLink() {
			messageUnderTest.setAudioLinkAsString("NOT A URL");
			assertThat(messageUnderTest.isValid(outStream)).isFalse();
			assertThat(validationMessage.toString()).contains("no protocol");
		}

		@Test
		public void shouldAcceptSpecialLinks() {
			messageUnderTest.setAudioLinkAsString("-");
			assertThat(messageUnderTest.isValid(outStream)).isTrue();

			messageUnderTest.setAudioLinkAsString("n/a");
			assertThat(messageUnderTest.isValid(outStream)).isTrue();

			messageUnderTest.setAudioLinkAsString("n/e");
			assertThat(messageUnderTest.isValid(outStream)).isTrue();

			messageUnderTest.setAudioLinkAsString("abrogated");
			assertThat(messageUnderTest.isValid(outStream)).isTrue();

			messageUnderTest.setAudioLinkAsString("rendering");
			assertThat(messageUnderTest.isValid(outStream)).isTrue();

			messageUnderTest.setAudioLinkAsString("flash");
			assertThat(messageUnderTest.isValid(outStream)).isTrue();

		}

		@Test
		public void shouldHaveValidVideoLink() {
			messageUnderTest.setVideoLinkAsString("NOT A URL");
			assertThat(messageUnderTest.isValid(outStream)).isFalse();
			assertThat(validationMessage.toString()).contains("no protocol");
		}

	}

}
