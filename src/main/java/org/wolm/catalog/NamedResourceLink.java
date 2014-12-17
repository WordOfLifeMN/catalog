package org.wolm.catalog;

import java.net.MalformedURLException;
import java.net.URL;

import org.wolm.message.Message;
import org.wolm.series.Series;

/**
 * A named resource link with a special property of containing a reference to a Series or Message that the resource came
 * from
 * 
 * @author wolm
 */
public class NamedResourceLink extends NamedLink {

	/**
	 * Series that the resource was attached to. Note that a message can be in multiple series, so this value may be
	 * just one of them at random
	 */
	private Series sourceSeries;

	/**
	 * Optional message that the resource was attached to. If {@code null} then the resource was from a series and not a
	 * message
	 */
	private Message sourceMessage;

	public NamedResourceLink(String name, URL link, Series sourceSeries) {
		super(name, link);
		this.sourceSeries = sourceSeries;
	}

	public NamedResourceLink(String name, URL link, Message sourceMessage) {
		super(name, link);
		this.sourceMessage = sourceMessage;
	}

	public NamedResourceLink(String s, Series sourceSeries) throws MalformedURLException {
		super(s);
		this.sourceSeries = sourceSeries;
	}

	public NamedResourceLink(String s, Message sourceMessage) throws MalformedURLException {
		super(s);
		this.sourceMessage = sourceMessage;
	}

	public Series getSourceSeries() {
		return sourceSeries;
	}

	public void setSourceSeries(Series sourceSeries) {
		this.sourceSeries = sourceSeries;
	}

	public Message getSourceMessage() {
		return sourceMessage;
	}

	public void setSourceMessage(Message sourceMessage) {
		this.sourceMessage = sourceMessage;
	}

}
