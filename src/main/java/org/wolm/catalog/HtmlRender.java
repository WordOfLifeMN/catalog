package org.wolm.catalog;

/**
 * Base class for renderers that produce HTML component output
 * 
 * @author wolm
 */
public abstract class HtmlRender {
	public static String baseUrl = "file:///Users/wolm/tmp/";

	public abstract String render() throws Exception;

}
