package org.wolm.catalog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;

/**
 * Manages a page hosted by the Weebly site. Given a URL to a page, can
 * <ul>
 * <li>Download and store the page
 * <li>Update references so it can run on a non-Weebly site (changes relative references to absolute)
 * <li>Substitute parts of the page with other parts
 * <li>Output the page in a location independent way
 * </ul>
 * 
 * @author kmurray
 */
public class WeeblyPage {
	public static final String DEV_NULL_URL = "file:///dev/null";

	/** URL of the page being loaded */
	private final URL url;

	/** Contents of the Weebly page */
	private List<String> lines;

	public static enum SubstitutePolicy {
		keep, remove, comment
	};
	private SubstitutePolicy unresolvedVariablePolicy = SubstitutePolicy.comment;

	/**
	 * Constructs a page by downloading a page from the Weebly servers.
	 * <p>
	 * This also accepts the magic DEV_NULL_URL that causes the Weebly Page to be constructed, but with no contents. You
	 * can then use {@link #setLines(List)} to populate the page with custom data (typically used for unit testing). It
	 * is recommended that you use the constructor that takes a list of lines instead, though
	 * 
	 * @param weeblyUrl URL to a page on the weebly servers
	 */
	public WeeblyPage(@Nonnull URL weeblyUrl) throws Exception {
		super();
		this.url = weeblyUrl;
		readPage();
	}

	/**
	 * Constructs a Weebly Page that contains the specified lines instead of being read from a URL
	 * 
	 * @param lines Lines to initialize the page with
	 * @throws Exception
	 */
	public WeeblyPage(List<String> lines) throws Exception {
		super();
		this.url = null;
		this.lines = lines;
		validatePage();
	}

	/**
	 * @return All the lines read for the page
	 */
	public List<String> getLines() {
		return lines;
	}

	/**
	 * Reads the page contents from the URL this class was constructed with. Keeps the content in memory
	 * 
	 * @throws IOException
	 */
	private void readPage() throws Exception {
		lines = new ArrayList<>(400); // empty Weebly page is about 220 lines

		// create a connection for the page
		URLConnection connection = url.openConnection();
		// without a user-agent, Weebly seems to assume this is a mobile device, so tell it not to use mobile
		connection.setRequestProperty("Cookie", "disable_mobile=1; is_mobile=0;");
		connection.connect();

		// read the page
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}

		// validate our assumptions about what a Weebly page looks like
		validatePage();
	}

	/**
	 * Validate some assumptions about the formatting and content of the Weebly page. If these assumptions cannot be
	 * validated, then some of the processing we will do later may not succeed, so we might as well fail now with a
	 * clear message
	 * 
	 * @throws Exception if any of our assumptions are invalid
	 */
	void validatePage() throws Exception {
		boolean hasHeader = false;
		boolean hasBody = false;
		@SuppressWarnings("unused")
		boolean inHeader = false;
		@SuppressWarnings("unused")
		boolean inBody = false;

		int index = 0;
		for (String line : lines) {
			index++;

			// check header and body
			if (line.contains("<head ") || line.contains("<head>")) inHeader = hasHeader = true;
			if (line.contains("</head>")) inHeader = false;
			if (line.contains("<body ") || line.contains("<body>")) inBody = hasBody = true;
			if (line.contains("</body>")) inBody = false;

			// link elements must have href attributes on same line
			int linkCount = StringUtils.countMatches(line, "<link ");
			if (linkCount > 0 && linkCount != StringUtils.countMatches(line, "href=")) {
				throw new Exception("Line " + index + " appears to have a <link> element broken across multiple lines");
			}
		}

		// check global assumptions
		if (!hasHeader) throw new Exception("No <head> element found");
		if (!hasBody) throw new Exception("No <body> element found");
	}

	/**
	 * Handles converting all relative references in the web page to absolute
	 */
	void preparePageForRemoteHosting() {
		boolean inHead = false;
		for (ListIterator<String> iter = lines.listIterator(); iter.hasNext();) {
			String line = iter.next();

			// convert relative references to absolute
			iter.set(convertRelativeLinkHrefToAbsolute(line));

			// add a base href in the header
			if (line.matches("\\s*<head>\\s*")) {
				iter.add("<base href=\"http://www.wordoflifemn.org/\" />");
				inHead = true;
			}

			if (inHead) {
				// replace variables with non-variable string
				String newLine = line.replaceAll("\\$\\{([^\\}]*)\\}", "$1");
				if (newLine != line) iter.set(newLine);
			}

			if (line.matches("\\s*</head>\\s*")) inHead = false;
		}
	}

	/**
	 * Converts all relative references in one line to absolute
	 * 
	 * @param lineToConvert Line with possible relative href references
	 * @return The input line with relative hrefs updated to absolute
	 */
	String convertRelativeLinkHrefToAbsolute(String lineToConvert) {
		// replaces '//*.com' with 'http://*.com'
		// replaces "//*.com" with "http://*.com"
		return lineToConvert.replaceAll("(['\"])//([\\w]*\\.[\\w]*\\.com)", "$1http://$2");
	}

	/**
	 * Prints the Weebly page to an output stream
	 * 
	 * @param out Print stream to output the page to
	 */
	public void printPage(PrintStream out) {
		for (String line : lines)
			out.println(line);
	}

	/** The policy for handling unresolved variables */
	public SubstitutePolicy getUnresolvedVariablePolicy() {
		return unresolvedVariablePolicy;
	}

	/** Change the policy for handling unresolved variables */
	public void setUnresolvedVariablePolicy(SubstitutePolicy unresolvedVariablePolicy) {
		this.unresolvedVariablePolicy = unresolvedVariablePolicy;
	}

	/**
	 * Finds variables and replaces them with their values. Variables start with "${" and end with "}", and between is
	 * the variable name. Varible names must contain no spaces, and be limited to alphanumeric characters. Disposition
	 * of unresolved variables is controlled by the {@link #setUnresolvedVariablePolicy()} setting
	 * 
	 * @param map Map of variable names to values
	 */
	public void substituteVariables(Map<String, String> map) {
		StrSubstitutor substitutor = new StrSubstitutor(map);
		for (ListIterator<String> iter = lines.listIterator(); iter.hasNext();) {
			String line = iter.next();
			String updated = substitutor.replace(line);
			if (!updated.equals(line)) iter.set(updated);
		}
	}

}
