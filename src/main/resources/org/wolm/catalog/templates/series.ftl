<h2>${series.title}</h2>
<table>
	<#-- cover art and description -->
	<tr>
		<#if series.coverArtLink??>
			<td valign="top"><img src="${series.coverArtLink}" width="128"/></td>
		</#if>
		<td valign="top" <#if !series.coverArtLink??>colspan="2"</#if>>
			<b>${series.title}</b><br/>
			${series.description!}
		</td>
	</tr>
	
	<#-- speaker -->
	<#if series.speakers?size &gt; 0>
		<tr>
			<td><#if series.speakers?size == 1>Speaker<#else>Speakers</#if>:</td>
			<td><#list series.speakers as speaker>${speaker}<#if speaker_has_next>, </#if></#list></td>
		</tr>
	</#if>
	
	<tr>
		<td>
			<#if !(series.startDate??) || !(series.endDate??) || series.endDate?date == series.startDate?date>
				Date:
			<#else>
				Dates:
			</#if>
		</td>
		<td>
			<#if series.startDate??>
				${series.startDate?date}
				<#if series.endDate?? && series.endDate?date != series.startDate?date>
					- ${series.endDate?date}
				</#if>
			</#if>
			(${series.messageCount} <#if series.messageCount == 1>message<#else>messages</#if>)
		</td>
	</tr>
</table>
