<p>Series List</p>
<ul>
	<#list catalog.series as series>
		<#if ((series.visibility)!'PRIVATE') == 'PUBLIC'>
			<li>
				<a href="${baseRef}/${series.id}.html">${series.title}</a>
				- ${series.messageCount} <#if series.messageCount == 1>message<#else>messages</#if>
				(${series.startDate?date} - ${series.endDate?date})
				<#-- (${series.startDate?date?string.iso} - ${series.endDate?date?string.iso}) -->
			</li>
		</#if>
	</#list>
</ul>
