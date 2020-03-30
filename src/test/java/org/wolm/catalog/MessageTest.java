package org.wolm.catalog;

import static org.fest.assertions.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
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
		private Message sut = null;
		private ByteArrayOutputStream validationMessage = null;
		private PrintStream outStream = null;

		@Before
		public void beforeEachTest() throws Exception {
			sut = new Message();
			sut.setTitle("UNIT TEST");
			sut.setDate(new Date());
			sut.setSeries(Arrays.asList(new String[] { "ONE", "TWO" }));
			sut.setTrackNumbers(Arrays.asList(new Integer[] { 1, 2 }));
			sut.setMinistry("WOL");
			sut.setType("Message");
			sut.setVisibilityAsString("Private");
			sut.setAudioLink(new URL("http://audio.com/link.wav"));
			sut.setVideoLink(new URL("http://video.com/link.mov"));

			validationMessage = new ByteArrayOutputStream();
			outStream = new PrintStream(validationMessage);
		}

		@After
		public void afterEachTest() {
			sut = null;
			outStream.close();
			validationMessage = null;
			outStream = null;
		}

		@Test
		public void shouldBeValid() {
			assertThat(sut.isValid(outStream)).isTrue();
			assertThat(validationMessage.toString()).isEmpty();
		}

		@Test
		public void shouldHaveTitle() {
			sut.setTitle(null);
			assertThat(sut.isValid(outStream)).isFalse();
			assertThat(validationMessage.toString()).contains("has no title");
		}

		@Test
		public void shouldHaveDate() {
			sut.setDate(null);
			assertThat(sut.isValid(outStream)).isFalse();
			assertThat(validationMessage.toString()).contains("has no date");
		}

		@Test
		public void ministryIsMissing() {
			sut.setMinistry(null);
			assertThat(sut.isValid(outStream)).isFalse();
			assertThat(validationMessage.toString()).contains("has no ministry");
		}

		@Test
		public void ministryIsInvalid() {
			sut.setMinistry("TESTING");
			assertThat(sut.isValid(outStream)).isFalse();
			assertThat(validationMessage.toString()).contains("unrecognized ministry");
		}

		@Test
		public void shouldHaveTracksToMatchSeries() {
			sut.setSeries(null);
			sut.setTrackNumbers(Arrays.asList(new Integer[] { 1 }));
			assertThat(sut.isValid(outStream)).isFalse();
			assertThat(validationMessage.toString()).contains("0 series, but has track data for 1");

			sut.setSeries(Arrays.asList(new String[] { "ONE", "TWO" }));
			sut.setTrackNumbers(Arrays.asList(new Integer[] { 1 }));
			assertThat(sut.isValid(outStream)).isFalse();
			assertThat(validationMessage.toString()).contains("2 series, but has track data for 1");
		}

		@Test
		public void hyphenSeriesShouldValidateWithoutTrack() {
			sut.setSeries(Arrays.asList(new String[] { "-" }));
			sut.setTrackNumbers(null);
			assertThat(sut.isValid(outStream)).isTrue();
		}

		@Test
		public void shouldHaveValidType() {
			sut.setType("TESTING");
			assertThat(sut.isValid(outStream)).isTrue(); // bad type is a warning, not an error
			assertThat(validationMessage.toString()).contains("unknown type");
		}

		@Test
		public void shouldHaveValidVisibility() {
			sut.setVisibilityAsString("VISIBLE");
			assertThat(sut.isValid(outStream)).isFalse();
			assertThat(validationMessage.toString()).contains("unknown visibility");
		}

		@Test
		public void shouldHaveValidAudioLink() {
			sut.setAudioLinkAsString("NOT A URL");
			assertThat(sut.isValid(outStream)).isFalse();
			assertThat(validationMessage.toString()).contains("no protocol");
		}

		@Test
		public void shouldAcceptSpecialLinks() {
			sut.setAudioLinkAsString("-");
			assertThat(sut.isValid(outStream)).isTrue();

			sut.setAudioLinkAsString("n/a");
			assertThat(sut.isValid(outStream)).isTrue();

			sut.setAudioLinkAsString("n/e");
			assertThat(sut.isValid(outStream)).isTrue();

			sut.setAudioLinkAsString("abrogated");
			assertThat(sut.isValid(outStream)).isTrue();

			sut.setAudioLinkAsString("rendering");
			assertThat(sut.isValid(outStream)).isTrue();

			sut.setAudioLinkAsString("flash");
			assertThat(sut.isValid(outStream)).isTrue();

		}

		@Test
		public void shouldHaveValidVideoLink() {
			sut.setVideoLinkAsString("NOT A URL");
			assertThat(sut.isValid(outStream)).isFalse();
			assertThat(validationMessage.toString()).contains("no protocol");
		}

	}

	public static class StandAlone {
		private Message sut = null;

		@Before
		public void beforeEachTest() throws Exception {
			sut = new Message();
			sut.setTitle("UNIT TEST");
			sut.setDate(new Date());
			sut.setSeries(Arrays.asList(new String[] { "ONE", "TWO" }));
			sut.setTrackNumbers(Arrays.asList(new Integer[] { 1, 2 }));
			sut.setMinistry("WOL");
			sut.setType("Message");
			sut.setVisibilityAsString("Private");
			sut.setAudioLink(new URL("http://audio.com/link.wav"));
			sut.setVideoLink(new URL("http://video.com/link.mov"));
		}

		@After
		public void afterEachTest() {
			sut = null;
		}

		@Test
		public void messageWithoutSeriesIsStandAlone() {
			sut.setSeries(Collections.<String> emptyList());
			assertThat(sut.isStandAlone()).isTrue();
		}

		@Test
		public void messageWithSeriesIsNotStandAlone() {
			assertThat(sut.isStandAlone()).isFalse();
		}

		@Test
		public void messageWith_SAM_IsStandAlone() {
			sut.setSeries(Arrays.asList(new String[] { "ONE", "SAM" }));
			assertThat(sut.isStandAlone()).isTrue();

			sut.setSeries(Arrays.asList(new String[] { "ONE", "Stand Alone" }));
			assertThat(sut.isStandAlone()).isTrue();

			sut.setSeries(Arrays.asList(new String[] { "ONE", "Stand Alone Message" }));
			assertThat(sut.isStandAlone()).isTrue();
		}

		@Test
		public void standAloneIsCaseInsensitive() {
			sut.setSeries(Arrays.asList(new String[] { "ONE", "sam" }));
			assertThat(sut.isStandAlone()).isTrue();

			sut.setSeries(Arrays.asList(new String[] { "ONE", "StAnd AlOnE" }));
			assertThat(sut.isStandAlone()).isTrue();

			sut.setSeries(Arrays.asList(new String[] { "ONE", "STaND ALoNe MeSSaGe" }));
			assertThat(sut.isStandAlone()).isTrue();
		}
	}

}
