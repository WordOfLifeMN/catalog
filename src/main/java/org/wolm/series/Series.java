package org.wolm.series;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.wolm.catalog.AccessLevel;
import org.wolm.catalog.NamedLink;
import org.wolm.catalog.RenderFactory;
import org.wolm.message.Message;

/**
 * Stores one series of messages
 * 
 * @author wolm
 */
public class Series {
	private String id;
	private String title;
	private Date startDate;
	private Date endDate;
	private Long messageCount;
	private List<String> speakers;
	private String description;
	private AccessLevel visibility;
	private URL coverArtLink;
	private URL coverImageLink;
	private List<NamedLink> studyGuides;

	transient private String visibilityError;
	transient private String coverArtLinkError;
	transient private String coverImageLinkError;
	transient private String studyGuideError;

	/** Messages for this series */
	private List<Message> messages = null;

	private static final List<String> SPECIAL_LINKS = Arrays.asList(new String[] { "-", "n/a", "n/e", "abrogated",
			"in progress", "editing", "rendering", "rendered", "flash", "uploading" });

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
		if (messageCount != null) return messageCount;
		if (messages != null) return (long) messages.size();
		return null;
	}

	public void setMessageCount(Long messageCount) {
		this.messageCount = messageCount;
	}

	public List<String> getSpeakers() {
		if (speakers == null) return Collections.emptyList();
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

	public List<NamedLink> getStudyGuides() {
		return studyGuides;
	}

	public void setStudyGuides(List<NamedLink> studyGuides) {
		this.studyGuides = studyGuides;
	}

	public void setStudyGuidesAsString(String serializedString) {
		studyGuides = null;
		if (serializedString == null) return;
		if (SPECIAL_LINKS.contains(serializedString)) return;

		String[] links = serializedString.split("\\s*;\\s*");
		if (links == null) return;

		studyGuides = new ArrayList<>();
		for (String link : links)
			try {
				studyGuides.add(new NamedLink(link));
			}
			catch (MalformedURLException e) {
				if (studyGuideError == null) studyGuideError = "unable to parse the study guide URLs: ";
				studyGuideError += "'" + link + "' (" + e.getMessage() + ")";
			}
	}

	/**
	 * @return Messages for this series that are visible under the current visibility rules
	 */
	public List<Message> getMessages() {
		if (messages == null) return null;
		List<Message> visibleMessages = new ArrayList<>(messages.size());

		for (Message message : messages)
			if (RenderFactory.isVisible(message.getVisibility())) visibleMessages.add(message);

		return visibleMessages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	public void addMessage(Message message) {
		if (messages == null) messages = new ArrayList<>();
		messages.add(message);
	}

	public boolean isValid(PrintStream s) {
		boolean valid = true;
		boolean needsHeader = true;
		int tmpCount;

		if (getId() == null) {
			printValidationError(s, needsHeader, "has no identifier");
			valid = needsHeader = false;
		}

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
			needsHeader = false;
		}
		else if (getMessageCount() < 1) {
			printValidationError(s, needsHeader, "has 0 messages");
			valid = needsHeader = false;
		}
		else if (messages == null || getMessageCount() > messages.size()) {
			printValidationError(s, needsHeader, "has a message count of " + getMessageCount() + " messages, but "
					+ (messages == null ? 0 : messages.size()) + " actual messages");
			valid = needsHeader = false;
		}
		else if (getMessageCount() > (tmpCount = countOfMessagesWithLessVisibilityThan(getVisibility()))) {
			printValidationError(s, needsHeader, "has visibility of " + getVisibility() + ", but " + tmpCount + " of "
					+ getMessageCount() + " are not visible at that level, some messages may not be displayed");
			// this is not a validation error, just a friendly warning - expected if the series is still in progress
			needsHeader = false;
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

		if (studyGuideError != null) {
			printValidationError(s, needsHeader, studyGuideError);
			valid = needsHeader = false;
		}

		return valid;
	}

	/**
	 * Counts the number of messages with less visibility than a specified level
	 * 
	 * @param visibilityCutoff
	 * @return Number of messages with less visibility than the {@code visibilityCutoff}
	 */
	private int countOfMessagesWithLessVisibilityThan(AccessLevel visibilityCutoff) {
		int count = 0;

		for (Message message : messages)
			if (AccessLevel.isLevelLessVisibleThanCutoff(message.getVisibility(), getVisibility())) count++;

		return count;
	}

	private void printValidationError(PrintStream s, boolean needsHeader, String error) {
		if (s == null) return;

		if (needsHeader) s.println("Series '" + getTitle() + "' has the following problems:");
		s.println("    * " + error);
	}

	/**
	 * Given the set of all messages, will find all the messages that are in this series and remember them for future
	 * use
	 * 
	 * @param messages Collection of all messages there are
	 * @return Number of messages found for this series
	 */
	public int discoverMessages(Collection<Message> allMessages) {
		messages = new ArrayList<>();

		// find all messages that are in this series
		for (Message message : allMessages) {
			if (message.getTrackNumber(getTitle()) == null) continue;
			messages.add(message);
		}

		// sort messages by track number
		Collections.sort(messages, new Message.ByTrackNumber(getTitle()));

		return messages.size();
	}

	@Override
	public String toString() {
		DateFormat fmt = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
		return getTitle() + " (" + fmt.format(getStartDate()) + "-"
				+ (getEndDate() == null ? "" : fmt.format(getEndDate())) + ") " + getMessageCount() + " messages.";
	}

	/** Comparator that sorts series by date, oldest to newest (<code>null</code> at end) */
	public static Comparator<Series> byDate = new Comparator<Series>() {
		public int compare(Series series1, Series series2) {
			Date date1 = series1.getStartDate();
			Date date2 = series2.getStartDate();
			if (date1 == null && date2 == null) return 0;
			if (date1 == null) return 1;
			if (date2 == null) return -1;
			return date1.before(date2) ? -1 : (date1.equals(date2) ? 0 : 1);
		}
	};

	/** Comparator that sorts series by date, newest to oldest (<code>null</code> at end) */
	public static Comparator<Series> byDateDescending = new Comparator<Series>() {
		public int compare(Series series1, Series series2) {
			Date date1 = series1.getStartDate();
			Date date2 = series2.getStartDate();
			if (date1 == null && date2 == null) return 0;
			if (date1 == null) return 1;
			if (date2 == null) return -1;
			return date1.before(date2) ? 1 : (date1.equals(date2) ? 0 : -1);
		}
	};
}
