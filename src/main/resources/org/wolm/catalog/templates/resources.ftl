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
	table {
	  border-collapse: collapse;
	}
	tr.reference {
		border-bottom: 1px solid #121;
	}
	td {
		vertical-align: middle;
	}
	td.resource {
		color: #bf9c03;
		text-align: left;
	}
	td.filename {
		color: #bf9c03;
		text-align:right;
	}
	div.source {
		padding-left: 36px;
		padding-right: 36px;
		color: #666;
		font-style: italic;
		font-size: 85%;
	}
	span.source {
		color: #999;
		font-size: 85%;
	}
	div.source a {
		color: #575;
	}
</style>

<hr />

<table class="resources" width="100%">
	<#list resourceList as resource>
		<tr class="resource">
			<td class="resource" width="60%">
				<a href="${resource.link}" target="wolmGuide">${resource.nameWithDateTrimmed}</a>
			</td>
			<td class="filename" width="40%">
				<#if resource.fileName??>
					<span class="source">(${resource.fileName})</span>
				</#if>
			</td>
		</tr>
		<tr class="reference">
			<td colspan="2">
				<#if (resource.sourceSeries.id)?? || resource.sourceMessage??>
					<#-- source of the resource -->
					<div class="source">
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
					</div>
				</#if>
			</td>
		</tr>
	</#list>
</table>
