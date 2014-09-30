package org.wolm.catalog;

/**
 * Base class for rederers that can render a URL to a particular thing
 * 
 * @author wolm
 */
public abstract class UrlRender {

	public abstract String render() throws Exception;
}
