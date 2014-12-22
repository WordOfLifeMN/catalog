package org.wolm.catalog;

import static org.fest.assertions.Assertions.*;

import java.net.MalformedURLException;

import org.junit.Test;

public class NamedLinkTest {

	@Test
	public void wolmBucketUrlShouldBeDocument() throws MalformedURLException {
		assertThat(
				new NamedLink(
						"https://s3-us-west-2.amazonaws.com/wordoflife.mn.audio/StudyGuide/Pastors+1990+Dream.pdf")
						.isDocumentForDownload()).isTrue();
	}

	@Test
	public void documentUrlShouldHaveFileName() throws MalformedURLException {
		assertThat(
				new NamedLink(
						"https://s3-us-west-2.amazonaws.com/wordoflife.mn.audio/StudyGuide/Pastors+1990+Dream.pdf")
						.getFileName()).isEqualTo("Pastors+1990+Dream.pdf");
	}

	@Test
	public void youtubeUrlShouldNotBeDocument() throws MalformedURLException {
		assertThat(new NamedLink("http://youtu.be/ygUSyQptiLQ").isDocumentForDownload()).isFalse();
	}

	@Test
	public void youtubeUrlShouldHaveDescription() throws MalformedURLException {
		assertThat(new NamedLink("http://youtu.be/ygUSyQptiLQ").getFileName()).isEqualTo("YouTube video");
	}

	@Test
	public void siteUrlShouldNotBeDocument() throws MalformedURLException {
		assertThat(new NamedLink("http://www.endtime.com").isDocumentForDownload()).isFalse();
		assertThat(new NamedLink("http://www.endtime.com/").isDocumentForDownload()).isFalse();
	}

	@Test
	public void siteUrlShouldHaveDescription() throws MalformedURLException {
		assertThat(new NamedLink("http://www.endtime.com").getFileName()).isEqualTo("Website");
		assertThat(new NamedLink("http://www.endtime.com/").getFileName()).isEqualTo("Website");
	}

	@Test
	public void sitePageUrlShouldNotBeDocument() throws MalformedURLException {
		assertThat(new NamedLink("https://store.endtime.com/Understanding-the-Endtime-DVD").isDocumentForDownload())
				.isFalse();
	}

	@Test
	public void sitePageUrlShouldNotHaveDescription() throws MalformedURLException {
		assertThat(new NamedLink("https://store.endtime.com/Understanding-the-Endtime-DVD").getFileName()).isNull();
	}

}
