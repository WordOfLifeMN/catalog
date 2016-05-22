package org.wolm.message;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.StringUtils;
import org.wolm.aws.AwsS3Helper;
import org.wolm.catalog.AccessLevel;
import org.wolm.catalog.Keywords;
import org.wolm.catalog.NamedLink;
import org.wolm.catalog.NamedResourceLink;

/**
 * Stores one message from the message log
 * 
 * @author wolm
 */
public class Message {

	private Date date;
	private String title;
	private List<String> series = new ArrayList<>();
	private List<Integer> trackNumbers = new ArrayList<>();
	private String description;
	private String type;
	private AccessLevel visibility;
	private List<String> speakers;
	private URL audioLink;
	private URL videoLink;
	private List<NamedLink> resources = new ArrayList<>();

	private transient String audioLinkError;
	private transient String videoLinkError;
	private transient String visibilityError;
	private transient String resourceError;

	private transient boolean isValid;
	private transient boolean validationErrorHasBeenPrinted;

	private static final List<String> TYPES = Arrays.asList(new String[] { "Ask Pastor", "C.O.R.E.", "Message",
			"Prayer", "Q&A", "Song", "Special Event", "Testimony", "Training", "Word" });
	private static final List<String> SPECIAL_LINKS = Arrays.asList(new String[] { "-", "n/a", "n/e", "abrogated",
			"source", "in progress", "editing", "edited", "rendering", "rendered", "flash", "uploading" });

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
		if (series == null) series = Collections.emptyList();
		this.series = new ArrayList<>(series.size());

		for (String seriesName : series) {
			seriesName = StringUtils.trimToNull(seriesName);
			if (seriesName == null) continue;
			if (seriesName.equals("-")) continue;
			this.series.add(seriesName);
		}
	}

	public List<Integer> getTrackNumbers() {
		return trackNumbers;
	}

	/**
	 * @param seriesName Name of a series that this message is in
	 * @return Track number of this message in that series. <code>null</code> if not in the specified series
	 */
	public Integer getTrackNumber(String seriesName) {
		for (int index = 0; index < trackNumbers.size(); index++)
			if (series.get(index).equalsIgnoreCase(seriesName)) return trackNumbers.get(index);
		return null;
	}

	public void setTrackNumbers(List<Integer> trackNumbers) {
		this.trackNumbers = trackNumbers == null ? new ArrayList<Integer>() : trackNumbers;
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

	public AccessLevel getVisibility() {
		return visibility;
	}

	public void setVisibility(AccessLevel visibility) {
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
			this.visibility = AccessLevel.PUBLIC;
			break;
		case "Protected":
		case "protected":
			this.visibility = AccessLevel.PROTECTED;
			break;
		case "Private":
		case "private":
			this.visibility = AccessLevel.PRIVATE;
			break;
		case "Private (Raw)":
		case "private (raw)":
			this.visibility = AccessLevel.RAW;
			break;
		default:
			this.visibility = null;
			visibilityError = "unknown visibility '" + visibility + "'";
			break;
		}
	}

	public List<String> getSpeakers() {
		if (speakers == null) return Collections.emptyList();
		return speakers;
	}

	public void setSpeakers(List<String> speakers) {
		this.speakers = speakers;
	}

	public URL getAudioLink() {
		return audioLink;
	}

	public URL getAudioLinkForDownload() {
		return AwsS3Helper.instance().getSignedUrl(audioLink, AwsS3Helper.Disposition.download);
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

	public List<NamedLink> getResources() {
		return resources;
	}

	public void setResources(List<NamedLink> resources) {
		this.resources = resources == null ? new ArrayList<NamedLink>() : resources;
	}

	public void setResourcesAsString(String serializedString) {
		resources = new ArrayList<>();
		if (serializedString == null) return;
		if (SPECIAL_LINKS.contains(serializedString)) return;

		String[] links = serializedString.split("\\s*;\\s*");
		if (links == null) return;

		for (String link : links)
			try {
				resources.add(new NamedResourceLink(link, this));
			}
			catch (MalformedURLException e) {
				if (resourceError == null) resourceError = "unable to parse the resource URLs: ";
				resourceError += "'" + link + "' (" + e.getMessage() + ")";
			}
	}

	public Keywords getKeywords() {
		Keywords helper = new Keywords();

		helper.add(getTitle());
		helper.add(getSpeakers());

		return helper;
	}

	public boolean isValid(PrintStream s) {
		isValid = true;
		validationErrorHasBeenPrinted = false;

		if (getTitle() == null) {
			reportValidationError(s, "has no title");
		}

		if (getDate() == null) {
			reportValidationError(s, "has no date");
		}

		int seriesCount = getSeries().size();
		int trackCount = getTrackNumbers().size();
		if (seriesCount != trackCount) {
			reportValidationError(s,
					"is in " + seriesCount + " series, but has track data for " + trackCount + " series");
		}

		if (getType() != null && !TYPES.contains(getType())) {
			reportValidationWarning(s, "has an unknown type '" + getType() + "'");
		}

		if (audioLinkError != null) reportValidationError(s, audioLinkError);
		if (videoLinkError != null) reportValidationError(s, videoLinkError);
		if (visibilityError != null) reportValidationError(s, visibilityError);
		if (resourceError != null) reportValidationError(s, resourceError);

		return isValid;
	}

	/**
	 * Outputs a validation error and flags this message as invalid
	 * 
	 * @param s Stream to print error to
	 * @param error Error string to display. If <code>null</code>, this method does nothing
	 */
	private void reportValidationError(PrintStream s, String error) {
		if (s == null) return;
		reportValidationWarning(s, error);
		isValid = false;
	}

	/**
	 * Outputs a validation error and flags this message as invalid
	 * 
	 * @param s Stream to print error to
	 * @param error Error string to display. If <code>null</code>, this method does nothing
	 */
	private void reportValidationWarning(PrintStream s, String error) {
		if (s == null) return;

		if (!validationErrorHasBeenPrinted) {
			s.println("Message '" + getTitle() + "' has the following problems:");
			validationErrorHasBeenPrinted = true;
		}

		s.println("    * " + error);
	}

	/**
	 * Examines the message data, and normalizes (standardizes) anything necessary. You can assume the message is
	 * already validated by the time this is called.
	 */
	public void normalize() {
		// Update speaker names with titles
		for (ListIterator<String> iter = getSpeakers().listIterator(); iter.hasNext();) {
			switch (iter.next()) {
			case "Vern Peltz":
				iter.set("Pastor Vern Peltz");
				break;
			}

		}
	}

	@Override
	public String toString() {
		DateFormat fmt = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
		return getTitle() + " (" + fmt.format(getDate()) + ")";
	}

	/** Comparator that sorts Message by date, oldest to newest (<code>null</code> at end) */
	public static Comparator<Message> byDate = new Comparator<Message>() {
		public int compare(Message Message1, Message Message2) {
			Date date1 = Message1.getDate();
			Date date2 = Message2.getDate();
			if (date1 == null && date2 == null) return 0;
			if (date1 == null) return 1;
			if (date2 == null) return -1;
			return date1.before(date2) ? -1 : (date1.equals(date2) ? 0 : 1);
		}
	};

	/** Comparator that sorts Message by date, newest to oldest (<code>null</code> at end) */
	public static Comparator<Message> byDateDescending = new Comparator<Message>() {
		public int compare(Message Message1, Message Message2) {
			Date date1 = Message1.getDate();
			Date date2 = Message2.getDate();
			if (date1 == null && date2 == null) return 0;
			if (date1 == null) return 1;
			if (date2 == null) return -1;
			return date1.before(date2) ? 1 : (date1.equals(date2) ? 0 : -1);
		}
	};

	/** Compares two messages by their track number for a given series */
	public static class ByTrackNumber implements Comparator<Message> {
		private final String seriesName;

		public ByTrackNumber(String seriesName) {
			super();
			this.seriesName = seriesName;
		}

		public int compare(Message msg1, Message msg2) {
			Integer trackNumber1 = msg1.getTrackNumber(seriesName);
			Integer trackNumber2 = msg2.getTrackNumber(seriesName);

			if (trackNumber1 == null && trackNumber2 == null) return 0;
			if (trackNumber1 == null) return 1;
			if (trackNumber2 == null) return -1;
			return trackNumber1 - trackNumber2;
		}

	}
}
