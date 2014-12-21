package org.wolm.catalog;

import java.net.MalformedURLException;
import java.net.URL;
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

}
