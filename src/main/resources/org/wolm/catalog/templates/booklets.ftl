<h1>Index of Booklets</h1>
<p>
	This is an index of all booklets that Word of Life Ministries has produced.<br/> 
	These booklets are not associated with any message or series, but are intended to be independent resources.
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

				<#-- <hr/> -->
			</td>
		</tr>
	</#list>
</table>
