package org.wolm.catalog;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Stores one series of messages
 * 
 * @author wolm
 */
public class Series {
	private String title;
	private Date startDate;
	private Date endDate;
	private Long messageCount;
	private List<String> speakers;
	private String description;
	private Visibility visibility;
	private URL coverArtLink;
	private URL coverImageLink;
	private List<URL> studyGuideLinks;

	transient private String visibilityError;
	transient private String coverArtLinkError;
	transient private String coverImageLinkError;
	transient private String studyGuideLinkError;

	private static final List<String> SPECIAL_LINKS = Arrays.asList(new String[] { "-", "n/a", "n/e", "abrogated",
			"in progress", "editing", "rendering", "rendered", "flash", "uploading" });

	enum Visibility {
		PUBLIC, PROTECTED, PRIVATE,
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Long getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(Long messageCount) {
		this.messageCount = messageCount;
	}

	public List<String> getSpeakers() {
		return speakers;
	}

	public void setSpeakers(List<String> speakers) {
		this.speakers = speakers;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
	}

	public void setVisibilityAsString(String visibility) {
		if (visibility == null) {
			this.visibility = null;
			return;
		}

		switch (visibility) {
		case "Public":
		case "public":
			this.visibility = Visibility.PUBLIC;
			break;
		case "Protected":
		case "protected":
			this.visibility = Visibility.PROTECTED;
			break;
		case "Private":
		case "private":
			this.visibility = Visibility.PRIVATE;
			break;
		default:
			this.visibility = null;
			visibilityError = "unknown visibility '" + visibility + "'";
			break;
		}
	}

	public URL getCoverArtLink() {
		return coverArtLink;
	}

	public void setCoverArtLink(URL coverArt) {
		this.coverArtLink = coverArt;
	}

	public void setCoverArtLinkAsString(String link) {
		if (SPECIAL_LINKS.contains(link)) return;
		try {
			coverArtLink = link == null ? null : new URL(link);
		}
		catch (MalformedURLException e) {
			coverArtLinkError = "unable to parse cover art URL '" + link + "': " + e.getMessage();
		}
	}

	public URL getCoverImageLink() {
		return coverImageLink;
	}

	public void setCoverImageLink(URL coverImage) {
		this.coverImageLink = coverImage;
	}

	public void setCoverImageLinkAsString(String link) {
		if (SPECIAL_LINKS.contains(link)) return;
		try {
			coverImageLink = link == null ? null : new URL(link);
		}
		catch (MalformedURLException e) {
			coverImageLinkError = "unable to parse cover image URL '" + link + "': " + e.getMessage();
		}
	}

	public List<URL> getStudyGuideLinks() {
		return studyGuideLinks;
	}

	public void setStudyGuideLinks(List<URL> studyGuide) {
		this.studyGuideLinks = studyGuide;
	}

	public void setStudyGuideLinksAsString(String linkString) {
		studyGuideLinks = null;
		if (linkString == null) return;
		if (SPECIAL_LINKS.contains(linkString)) return;
		String[] links = linkString.split("\\s*;\\s*");
		if (links == null) return;

		studyGuideLinks = new ArrayList<>();
		for (String link : links)
			try {
				studyGuideLinks.add(new URL(link));
			}
			catch (MalformedURLException e) {
				if (studyGuideLinkError == null) studyGuideLinkError = "unable to parse the study guide URLs: ";
				studyGuideLinkError += "'" + link + "' (" + e.getMessage() + ")";
			}
	}

	public boolean isValid(PrintStream s) {
		boolean valid = true;
		boolean needsHeader = true;

		if (getTitle() == null) {
			printValidationError(s, needsHeader, "has no title");
			valid = needsHeader = false;
		}

		if (getStartDate() == null) {
			printValidationError(s, needsHeader, "has no start date");
			valid = needsHeader = false;
		}

		if (getMessageCount() == null) {
			printValidationError(s, needsHeader, "has no message count, will be handled on a best effort basis");
			// this is not a validation error, just a friendly warning - expected if the series is still in progress
		}
		else if (getMessageCount() < 1) {
			printValidationError(s, needsHeader, "has 0 messages");
			valid = needsHeader = false;
		}

		if (visibilityError != null) {
			printValidationError(s, needsHeader, visibilityError);
			valid = needsHeader = false;
		}

		if (coverArtLinkError != null) {
			printValidationError(s, needsHeader, coverArtLinkError);
			valid = needsHeader = false;
		}

		if (coverImageLinkError != null) {
			printValidationError(s, needsHeader, coverImageLinkError);
			valid = needsHeader = false;
		}

		if (studyGuideLinkError != null) {
			printValidationError(s, needsHeader, studyGuideLinkError);
			valid = needsHeader = false;
		}

		return valid;
	}

	private void printValidationError(PrintStream s, boolean needsHeader, String error) {
		if (s == null) return;

		if (needsHeader) s.println("Series '" + getTitle() + "' has the following problems:");
		s.println("    * " + error);
	}

	@Override
	public String toString() {
		DateFormat fmt = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
		return getTitle() + " (" + fmt.format(getStartDate()) + "-"
				+ (getEndDate() == null ? "" : fmt.format(getEndDate())) + ") " + getMessageCount() + " messages.";
	}

	public String toHtml() {
		DateFormat fmt = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
		return "<b>" + getTitle() + "</b> - " + getMessageCount() + " messages (" + fmt.format(getStartDate()) + "-"
				+ (getEndDate() == null ? "" : fmt.format(getEndDate())) + ")";
	}

}
