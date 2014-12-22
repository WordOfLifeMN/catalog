<h1>Index of Booklets and Reference Materials</h1>
<p>
	This is an index of all booklets and reference materials that Word of Life Ministries has produced. 
	Where available, each booklet is cross-referenced with the series or message the it came from.
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
