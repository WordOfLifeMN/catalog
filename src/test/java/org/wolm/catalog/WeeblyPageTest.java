package org.wolm.catalog;

import static org.fest.assertions.Assertions.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class WeeblyPageTest {

	public static class ReadPage {
		@Test
		public void shouldReadHeaderFile() throws Exception {
			// find the test file
			URL headerUrl = this.getClass().getResource("WeeblyHeader.html");
			assertThat(headerUrl).isNotNull();
			assert headerUrl != null;

			WeeblyPage page = new WeeblyPage(headerUrl);
			assertThat(page.getLines().size()).isEqualTo(67);
		}
	}

	public static class ValidatePage {

		@Test(expected = Exception.class)
		public void emptyFileShouldNotValidate() throws Exception {
			new WeeblyPage(asList(new String[] {}));
		}

		@Test(expected = Exception.class)
		public void headerShouldNotBeMissing() throws Exception {
			new WeeblyPage(asList(new String[] { "<html>", "<body>", "</body>", "</html>" }));
		}

		@Test(expected = Exception.class)
		public void bodyShouldNotBeMissing() throws Exception {
			new WeeblyPage(asList(new String[] { "<html>", "<head>", "</head>", "</html>" }));
		}

		@Test
		public void shouldHaveBothHeaderAndBody() throws Exception {
			new WeeblyPage(asList(new String[] { "<html>", "<head>", "</head>", "<body>", "</body>", "</html>" }));
		}

		@Test
		public void linkAndHrefShouldBeOnSameLine() throws Exception {
			new WeeblyPage(
					asList(new String[] {
							"<html>",
							"<head>",
							"<link href='//fonts.googleapis.com/css?family=Lato:400,300,300italic,700,400italic,700italic' rel='stylesheet' type='text/css' />",
							"</head>", "<body>", "</body>", "</html>" }));
		}

		@Test(expected = Exception.class)
		public void linkAndHrefShouldNotBeOnDifferentLines() throws Exception {
			new WeeblyPage(
					asList(new String[] {
							"<html>",
							"<head>",
							"<link ",
							"href='//fonts.googleapis.com/css?family=Lato:400,300,300italic,700,400italic,700italic' rel='stylesheet' type='text/css' />",
							"</head>", "<body>", "</body>", "</html>" }));
		}

		@Test(expected = Exception.class)
		public void linkAndHrefShouldNotBeOnDifferentLines_Multiple() throws Exception {
			new WeeblyPage(
					asList(new String[] {
							"<html>",
							"<head>",
							"<link rel=\"stylesheet\" href=\"//cdn2.editmysite.com/css/sites.css?buildTime=1397689925\" type=\"text/css\" /><link rel='stylesheet' ",
							"type='text/css' href='//cdn1.editmysite.com/editor/libraries/fancybox/fancybox.css?1397603321' />",
							"</head>", "<body>", "</body>", "</html>" }));
		}
	}

	public static class PreparePageForRemoteHosting {

		@Test
		public void baseHrefShouldBeInsertedInHead() throws Exception {
			WeeblyPage page = new WeeblyPage(asList(new String[] { "<html>", "<head>", "</head>", "<body>", "</body>",
					"</html>" }));

			page.preparePageForRemoteHosting();

			assertThat(page.getLines().size()).isEqualTo(7);
			assertThat(page.getLines().get(1)).contains("<head>");
			assertThat(page.getLines().get(2)).contains("<base ");
			assertThat(page.getLines().get(3)).contains("</head>");
		}
	}

	public static class ConvertRelativeLinkHrefToAbsolute {
		private WeeblyPage page;

		@Before
		public void beforeEachTest() throws Exception {
			page = new WeeblyPage(
					asList(new String[] { "<html>", "<head>", "</head>", "<body>", "</body>", "</html>" }));
		}

		@Test
		public void noLinkShouldReturnInputLine() {
			String line = "THERE ARE NO LINKS IN THIS LINE";
			assertThat(page.convertRelativeLinkHrefToAbsolute(line) == line).isTrue();
		}

		@Test
		public void hostHrefShouldResolve() {
			assertThat(page.convertRelativeLinkHrefToAbsolute("<link href=\"//UNIT.TEST.com\"/>")).isEqualTo(
					"<link href=\"http://UNIT.TEST.com\"/>");
			assertThat(page.convertRelativeLinkHrefToAbsolute("<link href='//UNIT.TEST.com'/>")).isEqualTo(
					"<link href='http://UNIT.TEST.com'/>");
		}

		@Test
		public void multipleHrefShouldResolve() {
			String lineSource = "<link href=\"//UNIT.TESTA.com\"/><link href='//UNIT.TESTB.com'/>";
			String lineTarget = "<link href=\"http://UNIT.TESTA.com\"/><link href='http://UNIT.TESTB.com'/>";
			assertThat(page.convertRelativeLinkHrefToAbsolute(lineSource)).isEqualTo(lineTarget);
		}

		@Test
		public void absoluteHrefShouldNotChange() {
			String line = "<link href=\"http://www.wordoflifemn.org/about-us.html\"/>";
			assertThat(page.convertRelativeLinkHrefToAbsolute(line)).isEqualTo(line);

			line = "<link href='http://www.wordoflifemn.org/about-us.html'/>";
			assertThat(page.convertRelativeLinkHrefToAbsolute(line)).isEqualTo(line);
		}

	}

	/**
	 * Constructs a fully mutable ArrayList from an array of strings. Arrays.asList() creates an immutable array.
	 * 
	 * @param strings Strings to put into a list
	 * @return ArrayList of strings.
	 */
	private static List<String> asList(String[] strings) {
		ArrayList<String> array = new ArrayList<>(strings.length);
		for (String s : strings)
			array.add(s);
		return array;
	}

}
