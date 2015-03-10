package org.wolm.catalog;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.Objects;

/**
 * POJO for storing a link URL with a human readable name
 * 
 * @author wolm
 */
public class NamedLink {
	private final String name;
	private final URL link;

	public NamedLink(String name, URL link) {
		super();
		this.name = name;
		this.link = link;
	}

	/**
	 * Constructs a named link from a serialized string in one of the following formats:
	 * <ul>
	 * <li>link
	 * <li>name|link
	 * <li>link (name)
	 * <li>link &lt;name&gt;
	 * </ul>
	 * 
	 * @param s
	 */
	public NamedLink(String s) throws MalformedURLException {
		if (s.contains("|")) {
			int pos = s.indexOf('|');
			name = s.substring(0, pos).trim();
			link = new URL(s.substring(pos + 1).trim());
		}
		else if (s.contains("(") && s.contains(")")) {
			int open = s.indexOf('(');
			int close = s.indexOf(')', open);
			name = s.substring(open + 1, close).trim();
			link = new URL(s.substring(0, open).trim());
		}
		else if (s.contains("<") && s.contains(">")) {
			int open = s.indexOf('<');
			int close = s.indexOf('>', open);
			name = s.substring(open + 1, close).trim();
			link = new URL(s.substring(0, open).trim());
		}
		else {
			link = new URL(s);
			name = link.getPath().replaceFirst(".*/", "").replace('+', ' ').replaceFirst("\\.[^\\.]*$", "");
		}
	}

	public String getName() {
		return name;
	}

	public URL getLink() {
		return link;
	}

	/**
	 * @return The file name extracted from the URL. If this is a document ({@link #isDocumentForDownload()}), then this
	 * will be the downloadable file name, if not, then this will either be null or a human-readable description like
	 * "YouTube video" or "Website"
	 */
	public String getFileName() {
		String path = link.getPath();

		if (isDocumentForDownload()) {
			return path.substring(path.lastIndexOf('/') + 1); // works if lastIndexOf() returns -1
		}

		if (link.getHost().contains("youtu")) return "YouTube video";
		if (path.isEmpty() || path.equals("/")) return "Website";
		if (link.getHost().contains(".com")) return "Web page";
		return null;
	}

	/**
	 * Examines the link and determines if this looks like a document that someone could download from our site (like a
	 * picture or PDF). The alternative is that this might be an online resource (like a YouTube video or a guest
	 * speaker's website)
	 * <p>
	 * Heuristic: assume it is a document if it is in one of our Amazon AWS buckets (like
	 * "https://s3-us-west-2.amazonaws.com/wordoflife.mn.audio/StudyGuide/Pastors+1990+Dream.pdf") by looking for
	 * "amazonaws" and one of our bucket names
	 * 
	 * @return {@code true} if this looks like something that could be downloaded from our site. {@code false} otherwise
	 */
	public boolean isDocumentForDownload() {
		String url = link.toString();
		return url.contains("amazonaws") && url.contains("/wordoflife.mn.");
	}

	/**
	 * Returns {@code true} if this resource references a PDF file
	 * 
	 * @return {@code true} if this is a link to a PDF, {@code false} for anything else
	 */
	public boolean isPdf() {
		String url = link.toString();
		return url.endsWith(".pdf") || url.endsWith(".PDF");
	}

	@Override
	public int hashCode() {
		return Objects.hash(link, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		NamedLink other = (NamedLink) obj;
		return Objects.equals(link, other.link) && Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		return "NamedLink [name=" + name + "]";
	}

	public static Comparator<NamedLink> byName = new Comparator<NamedLink>() {
		public int compare(NamedLink link1, NamedLink link2) {
			return link1.getName().compareToIgnoreCase(link2.getName());
		}
	};

	/** Sorts by name, but ignoring leading "A" or "The" */
	public static Comparator<NamedLink> byTitleName = new Comparator<NamedLink>() {
		public int compare(NamedLink link1, NamedLink link2) {
			String title1 = link1.getName().replaceFirst("^(A|The) ", "");
			String title2 = link2.getName().replaceFirst("^(A|The) ", "");
			return title1.compareToIgnoreCase(title2);
		}
	};
}
