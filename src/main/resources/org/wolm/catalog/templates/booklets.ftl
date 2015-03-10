<h1>Index of Booklets</h1>
<p>
	This is an index of all booklets that Word of Life Ministries has produced.<br/> 
	These booklets are cross-referenced with the series that it was produced for, although 
	some booklets are not associated with any series, but are intended to be independent resources.
	To see a list of all our handouts and resources that accompany messages, see the 
	<a href="${baseRef}/resources.html">resources</a> page.
</p>

<style>
	td.resource .filename {
		color: #bf9c03;
		float: right;
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

<table class="resources" width="100%">
	<#list resourceList as resource>
		<tr>
			<td class="resource">
				<#if resource.fileName??>
					<span class="filename">(${resource.fileName})</span>
				</#if>
				<a href="${resource.link}" target="wolmGuide">${resource.name}</a>
				<span style="float:clear;" />

				<#-- source of the resource -->
				<br/>
				<span class="source">
					<#if (resource.sourceSeries.id)?? || resource.sourceMessage??>
						<#-- series and/or message names -->
						From 
						<#if (resource.sourceSeries.id)??>
							<a href="${baseRef}/${resource.sourceSeries.id}.html">${resource.sourceSeries.title}</a>
						</#if>
						<#if (resource.sourceSeries.id)?? && resource.sourceMessage??>:</#if>
						<#if resource.sourceMessage??>
							${resource.sourceMessage.title}
						</#if>
						
						<#-- source date -->
						<#if resource.sourceMessage??>
							<#if resource.sourceMessage.date??>(${resource.sourceMessage.date?date})</#if>
						<#elseif resource.sourceSeries??>
							<#if resource.sourceSeries.startDate??>(${resource.sourceSeries.startDate?date})</#if>
						</#if>
					<#else>
						Not associated with any series.
					</#if>
				</span>
				<#-- <hr/> -->
			</td>
		</tr>
	</#list>
</table>
