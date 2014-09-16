package org.wolm.catalog;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Stores one message from the message log
 * 
 * @author wolm
 */
public class Message {

	private Date date;
	private String title;
	private List<String> series;
	private List<Integer> trackNumbers;
	private String description;
	private String type;
	private Visibility visibility;
	private List<String> speakers;
	private URL audioLink;
	private URL videoLink;

	private transient String audioLinkError;
	private transient String videoLinkError;
	private transient String visibilityError;

	enum Visibility {
		PUBLIC, PROTECTED, PRIVATE, UNEDITED
	}

	private static final List<String> TYPES = Arrays.asList(new String[] { "C.O.R.E.", "Message", "Prayer", "Q&A",
			"Song", "Special Event", "Testimony", "Training", "Word" });
	private static final List<String> SPECIAL_LINKS = Arrays.asList(new String[] { "-", "n/a", "n/e", "abrogated",
			"in progress", "editing", "rendering", "rendered", "flash", "uploading" });

	public Message() {
		super();
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getSeries() {
		return series;
	}

	public void setSeries(List<String> series) {
		this.series = series;
	}

	public List<Integer> getTrackNumbers() {
		return trackNumbers;
	}

	public void setTrackNumbers(List<Integer> trackNumbers) {
		this.trackNumbers = trackNumbers;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
		case "Private (Raw)":
		case "private (raw)":
			this.visibility = Visibility.UNEDITED;
			break;
		default:
			this.visibility = null;
			visibilityError = "unknown visibility '" + visibility + "'";
			break;
		}
	}

	public List<String> getSpeakers() {
		return speakers;
	}

	public void setSpeakers(List<String> speakers) {
		this.speakers = speakers;
	}

	public URL getAudioLink() {
		return audioLink;
	}

	public void setAudioLink(URL audioLink) {
		this.audioLink = audioLink;
	}

	public void setAudioLinkAsString(String link) {
		if (link == null || SPECIAL_LINKS.contains(link)) return;
		try {
			audioLink = new URL(link);
		}
		catch (MalformedURLException e) {
			audioLinkError = "unable to parse audio URL '" + link + "': " + e.getMessage();
		}
	}

	public URL getVideoLink() {
		return videoLink;
	}

	public void setVideoLink(URL videoLink) {
		this.videoLink = videoLink;
	};

	public void setVideoLinkAsString(String link) {
		if (link == null || SPECIAL_LINKS.contains(link)) return;
		try {
			videoLink = new URL(link);
		}
		catch (MalformedURLException e) {
			videoLinkError = "unable to parse video URL '" + link + "': " + e.getMessage();
		}
	}

	public boolean isValid(PrintStream s) {
		boolean valid = true;
		boolean needsHeader = true;

		if (getTitle() == null) {
			printValidationError(s, needsHeader, "has no title");
			valid = needsHeader = false;
		}

		if (getDate() == null) {
			printValidationError(s, needsHeader, "has no date");
			valid = needsHeader = false;
		}

		int seriesCount = getSeries() == null ? 0 : getSeries().size();
		int trackCount = getTrackNumbers() == null ? 0 : getTrackNumbers().size();
		if (seriesCount != trackCount) {
			printValidationError(s, needsHeader, "is in " + seriesCount + " series, but has track data for "
					+ trackCount);
			valid = needsHeader = false;
		}

		if (getType() != null && !TYPES.contains(getType())) {
			printValidationError(s, needsHeader, "has an unknown type '" + getType() + "'");
			// this is not a validation problem, just a warning that there might be a typo
			needsHeader = false;
		}

		if (audioLinkError != null) {
			printValidationError(s, needsHeader, audioLinkError);
			valid = needsHeader = false;
		}

		if (videoLinkError != null) {
			printValidationError(s, needsHeader, videoLinkError);
			valid = needsHeader = false;
		}

		if (visibilityError != null) {
			printValidationError(s, needsHeader, visibilityError);
			valid = needsHeader = false;
		}

		return valid;
	}

	private void printValidationError(PrintStream s, boolean needsHeader, String error) {
		if (s == null) return;

		if (needsHeader) s.println("Message '" + getTitle() + "' has the following problems:");
		s.println("    * " + error);
	}

	@Override
	public String toString() {
		DateFormat fmt = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
		return getTitle() + " (" + fmt.format(getDate()) + ")";
	}

	public String toHtml() {
		DateFormat fmt = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
		return "<b>" + StringEscapeUtils.escapeHtml4(getTitle()) + "</b> (" + fmt.format(getDate()) + ")";
	}
}
