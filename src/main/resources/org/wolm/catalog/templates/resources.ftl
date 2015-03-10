<h1>Index of Message Reference Materials</h1>
<p>
	This is an index of handouts and reference materials that Word of Life Ministries has produced to accompany
	messages.<br/>
	Each resources is cross-referenced with the series or message that it was produced for. In many cases, the 
	material in these resources contains limited explanations, and you will need to listen or watch
	the associated message to get the most out of them.<br/> 
	For a list of the booklets that we have produced, please see the <a href="${baseRef}/booklets.html">booklets</a> page.
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

				<#if (resource.sourceSeries.id)?? || resource.sourceMessage??>
					<#-- source of the resource -->
					<br/>
					<span class="source">
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
					</span>
				</#if>
				<#-- <hr/> -->
			</td>
		</tr>
	</#list>
</table>
