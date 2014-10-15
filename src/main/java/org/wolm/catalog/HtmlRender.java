package org.wolm.catalog;

/**
 * Base class for renderers that produce HTML component output
 * 
 * @author wolm
 */
public abstract class HtmlRender extends Render {
	// TODO: How should we set this? can we generate it from Javascript, or is there a special markup that overrides the
	// base href?
	public static String baseUrl = "file:///Users/wolm/tmp/";

	public abstract String render() throws Exception;

}
