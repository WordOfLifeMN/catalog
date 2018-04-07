package org.wolm.prophesy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import org.wolm.catalog.Keywords;

public class Prophecy {
	private String id;
	private String title;
	private String by;
	private String location;
	private String dateString;
	private String htmlBody;

	public String getId() {
		if (id == null) {
			id = getDateString().replaceAll("[^0-9]", "");
		}
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

	public String getBy() {
		return by;
	}

	public void setBy(String by) {
		this.by = by;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDateString() {
		return dateString;
	}

	public Date getDate() {
		if (getDateString() == null) return null;
		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse(getDateString());
		}
		catch (ParseException e) {
			// ignore
		}
		return null;
	}

	public void setDateString(String date) {
		this.dateString = date;
	}

	public String getHtmlBody() {
		return htmlBody;
	}

	public void setHtmlBody(String htmlBody) {
		this.htmlBody = htmlBody;
	}

	public String getBody() {
		String body = getHtmlBody();
		body = body.replaceAll("<iframe.*youtube.*iframe>", "(audio or video)");
		body = body.replaceAll("</?[^<]*>", " ");
		body = body.replace("  *", " ");
		return body;
	}

	public Keywords getKeywords() {
		Keywords keywords = new Keywords();

		// add the title and body of the prophecy
		keywords.add(getTitle());
		keywords.add(getBody());

		// add the year of the prophecy
		keywords.add(getDateString().substring(0, 4));

		return keywords;
	}

	public static final Comparator<Prophecy> byDateDescending = new Comparator<Prophecy>() {
		public int compare(Prophecy o1, Prophecy o2) {
			Date d1 = o1.getDate();
			Date d2 = o2.getDate();
			if (d1 == null) {
				if (d2 == null) return 0;
				return 1;
			}

			if (d2 == null) return -1;
			return -1 * d1.compareTo(d2);
		}
	};
}
