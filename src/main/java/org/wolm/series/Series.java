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
import java.util.ListIterator;
import java.util.Set;

import org.wolm.catalog.AccessLevel;
import org.wolm.catalog.App;
import org.wolm.catalog.NamedLink;
import org.wolm.catalog.NamedResourceLink;
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
	private List<NamedLink> booklets = new ArrayList<>();
	private List<NamedLink> resources = new ArrayList<>();

	transient private String visibilityError;
	transient private String coverArtLinkError;
	transient private String coverImageLinkError;
	transient private String resourceError;
	transient private String bookletError;

	transient private boolean validationErrorHasBeenPrinted;
	transient private boolean isValid;

	/** Messages for this series */
	private List<Message> messages = new ArrayList<>();

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
		return (long) messages.size();
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

	/**
	 * Gets all booklets (listed in the "Booklets" column) regardless of visibility
	 * 
	 * @return List of all series booklets
	 */
	public List<NamedLink> getBooklets() {
		return booklets;
	}

	public void setBooklets(List<NamedLink> booklets) {
		this.booklets = booklets == null ? new ArrayList<NamedLink>() : booklets;
	}

	public void setBookletsAsString(String serializedBooklets) {
		booklets = new ArrayList<>();
		if (serializedBooklets == null) return;
		if (SPECIAL_LINKS.contains(serializedBooklets)) return;

		String[] links = serializedBooklets.split("\\s*;\\s*");
		if (links == null) return;

		for (String link : links)
			try {
				booklets.add(new NamedResourceLink(link, this));
			}
			catch (MalformedURLException e) {
				if (resourceError == null) resourceError = "unable to parse the booklet URLs: ";
				bookletError += "'" + link + "' (" + e.getMessage() + ")";
			}
	}

	/**
	 * @return Resources. Empty list if none
	 */
	public List<NamedLink> getResources() {
		return getResources(true);
	}

	public List<NamedLink> getResources(boolean includeResourcesFromMessages) {
		List<NamedLink> allResources = new ArrayList<>();
		allResources.addAll(booklets);
		allResources.addAll(resources);

		if (!includeResourcesFromMessages) return allResources;

		if (getMessages() != null) {
			for (Message message : getMessages()) {
				for (NamedLink resource : message.getResources()) {
					// set myself as the owning series, do this in a copy of the resource link so we don't mess up the
					// original
					NamedResourceLink messageResource = new NamedResourceLink(resource.getName(), resource.getLink(),
							message);
					messageResource.setSourceSeries(this);
					allResources.add(messageResource);
				}
			}
		}

		return allResources;
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

	/**
	 * @return Messages for this series that are visible under the current visibility rules
	 */
	public List<Message> getMessages() {
		List<Message> visibleMessages = new ArrayList<>(messages.size());

		for (Message message : messages)
			if (RenderFactory.isAtLeastVisible(message.getVisibility())) visibleMessages.add(message);

		return visibleMessages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages == null ? new ArrayList<Message>() : messages;
	}

	public void addMessage(Message message) {
		messages.add(message);
	}

	/**
	 * Determines if this series represents only a booklet and no messages. Series like this have no start date, no
	 * message count but do have a resource
	 * 
	 * @return {@code true} if this series is just a booklet, {@code false} if it includes messages
	 */
	public boolean isBooklet() {
		if (getTitle() == null) return false;
		if (getBooklets().isEmpty()) return false;
		if (getStartDate() != null) return false;
		if (getMessageCount() > 0) return false;
		return true;
	}

	/**
	 * Determins if this series is valid or not
	 * 
	 * @param s Series to validate
	 * @param seriesIds List of IDs of all the series. If this series' ID is in the list, then it is not unique and
	 * should be considered invalid. Upon return, this series' ID will be added to the list
	 * @return
	 */
	public boolean isValid(PrintStream s, Set<String> seriesIds) {
		isValid = true;
		validationErrorHasBeenPrinted = false;

		if (visibilityError != null) reportValidationError(s, visibilityError);
		if (coverArtLinkError != null) reportValidationError(s, coverArtLinkError);
		if (coverImageLinkError != null) reportValidationError(s, coverImageLinkError);
		if (bookletError != null) reportValidationError(s, resourceError);
		if (resourceError != null) reportValidationError(s, resourceError);

		if (getTitle() == null) {
			reportValidationError(s, "has no title");
		}

		// some series are just booklets and nothing else, so don't check stuff that doesn't matter for these
		if (isBooklet()) return isValid;

		// if a series has no visibility, then it has not been fully defined yet, so needs no further validation
		if (getVisibility() == null) return isValid;

		if (getId() == null) {
			reportValidationError(s, "has no identifier");
		}
		else if (seriesIds != null) {
			if (seriesIds.contains(getId())) {
				reportValidationError(s, "has an ID that is already in use by another series");
			}
			seriesIds.add(getId());
		}

		if (getStartDate() == null) {
			reportValidationError(s, "has no start date");
		}

		if (getMessageCount() == null) {
			reportValidationWarning(s, "has no message count, will be handled on a best effort basis");
		}
		else if (getMessageCount() < 1) {
			reportValidationError(s, "has 0 messages");
		}
		else if (getMessageCount() > messages.size()) {
			reportValidationError(s,
					"has a message count of " + getMessageCount() + " messages, but " + messages.size()
							+ " actual messages");
		}
		else {
			int tmpCount = countOfMessagesWithLessVisibilityThan(getVisibility());
			if (tmpCount > 0) {
				reportValidationWarning(s, "has visibility of " + getVisibility() + ", but " + tmpCount + " of "
						+ countOfMessagesWithAnyVisibility()
						+ " messages are not visible at that level, some messages may not be displayed");
			}
		}

		return isValid;
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

	private int countOfMessagesWithAnyVisibility() {
		return messages.size();
	}

	/**
	 * Outputs a validation error and flags this series as invalid
	 * 
	 * @param s Stream to print error to
	 * @param error Error string to display. If <code>null</code>, this method does nothing
	 */
	private void reportValidationError(PrintStream s, String error) {
		reportValidationWarning(s, error);
		isValid = false;
	}

	/**
	 * Outputs a validation error and flags this series as invalid
	 * 
	 * @param s Stream to print error to
	 * @param error Error string to display. If <code>null</code>, this method does nothing
	 */
	private void reportValidationWarning(PrintStream s, String error) {
		if (s == null) return;

		if (!validationErrorHasBeenPrinted) {
			s.println("Series '" + getTitle() + "' has the following problems:");
			validationErrorHasBeenPrinted = true;
		}

		s.println("    * " + error);
	}

	/**
	 * Examines the series data, and normalizes (standardizes) anything necessary. You can assume the series is already
	 * validated by the time this is called.
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

			App.logDebug("Adding " + message.getVisibility() + " message '" + message.getTitle() + "'");
			messages.add(message);
		}

		// sort messages by track number
		Collections.sort(messages, new Message.ByTrackNumber(getTitle()));

		return messages.size();
	}

	@Override
	public String toString() {
		DateFormat fmt = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM);
		StringBuilder b = new StringBuilder();

		b.append(getTitle());
		b.append(" ");
		if (getStartDate() != null) {
			b.append("(");
			b.append(fmt.format(getStartDate()));
			b.append("-");
			if (getEndDate() != null) b.append(fmt.format(getEndDate()));
			b.append(") ");
		}
		b.append(getMessageCount()).append(" messages.");
		return b.toString();
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

	/** Comparator that sorts series by title */
	public static Comparator<Series> byTitle = new Comparator<Series>() {
		public int compare(Series series1, Series series2) {
			return series1.getTitle().compareToIgnoreCase(series2.getTitle());
		}
	};
}
