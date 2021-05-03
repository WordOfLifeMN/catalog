<h1>Index of Booklets</h1>
<p>
	This is an index of all booklets that Word of Life Ministries has produced.<br/> 
	These booklets are cross-referenced with the series that it was produced for, although 
	some booklets are not associated with any series, but are intended to be independent resources.
	To see a list of all our handouts and resources that accompany messages, see the 
	<a href="${baseRef}/resources.html">resources</a> page.
</p>
<p>
	<b>Key:</b><br/>
	&nbsp;&nbsp;&nbsp;
	<img src="https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/WordofLifeLogo-XXSmall.png" alt="Word of Life Logo"/>
	Indicates a booklet associated with a series. Click on this icon to see the series.
	<br/>
	&nbsp;&nbsp;&nbsp;
	<img src="https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/CombBound-XXSmall.gif"  alt="Booklet Logo"/>
	Indicates a booklet not currently associated with a series.
</p>

<style>
	table {
	  border-collapse: collapse;
	}
	tr.booklet {
		border-bottom: 1px solid #121;
	}
	td {
		vertical-align: middle;
	}
	td.resource {
		color: #bf9c03;
	}
	td.filename {
		color: #bf9c03;
		text-align: right;
	}
	span.source {
		padding-left: 24px;
		color: #777;
		font-style: italic;
		font-size: 85%;
	}
	span.source a {
		color: #797;
	}
</style>

<hr />
<table class="resources" width="100%">
	<#list resourceList as resource>
		<tr class="booklet">
			<td class="icon">
				<#if (resource.sourceSeries.id)?? || resource.sourceMessage??>
					<#assign alt= 'From ' />
					<#if (resource.sourceSeries.id)??><#assign alt = alt + resource.sourceSeries.title /></#if>
					<#if (resource.sourceSeries.id)?? && resource.sourceMessage??><#assign alt = alt + ':' /></#if>
					<#assign alt = alt + ' ' />
					<#if resource.sourceMessage??><#assign alt = alt + resource.sourceMessage.title /></#if>
					
					<#-- source date -->
					<#if resource.sourceMessage??>
						<#assign alt = alt + ' (' + resource.sourceMessage.date?date + ')' />
					<#elseif resource.sourceSeries??>
						<#assign alt = alt + ' (' + resource.sourceSeries.startDate?date + ')' />
					</#if>
					<a href="${baseRef}/${resource.sourceSeries.id}.html">
						<img src="https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/WordofLifeLogo-XXSmall.png" 
							alt="${alt}" title="${alt}"/>
					</a>
				<#else>
					<#assign alt= 'This booklet is not associated with any series.' />
					<img src="https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/CombBound-XXSmall.gif" 
						alt="${alt}" title="${alt}"/>
				</#if>
			</td>
			<td class="resource">
				<a href="${resource.link}" target="wolmGuide">${resource.name}</a>
			</td>
			<td class="filename">
				<#if resource.fileName??>
					<span class="source">(${resource.fileName})</span>
				<#else>
					&nbsp;
				</#if>
			</td>
		</tr>
	</#list>
</table>
