<style>
	<#if department! == 'CORE'>
		a { color: #840; }
		a:hover { color: #c70; }
	</#if>
	span.inprogress { color: #999; font-style: italic; }
</style>

<#macro seriesSummaryItem series>
	<li>
		<a href="${baseRef}/${series.id}.html">${series.title}</a>
		- ${series.messageCount} <#if series.messageCount == 1>message<#else>messages</#if>
		(
			<#if series.startDate??>
				<#if !(series.endDate??)>Started</#if> <#-- still in progress -->
				${series.startDate?date}
				<#if series.endDate?? && series.endDate?date != series.startDate?date>
					- ${series.endDate?date}
				</#if>
				<#if !(series.endDate??)><span class="inprogress"> -more to come!</span></#if>
			</#if>
		)
		<#-- (${series.startDate?date?string.iso} - ${series.endDate?date?string.iso}) -->
	</li>
</#macro>

<#-- -------------------------------------------------------------------------------------- -->

<h1>${title!}</h1>

<#if promoFileName??>
	<div style="border-style:ridge;border-color:#5a9e5d;border-width:medium;padding:4px;margin:32px;">
		<#include "${promoFileName}"/>
	</div>
</#if>

<#if description??><p>${description}</p></#if>
<ul>
	<#list seriesList as series>
		<@seriesSummaryItem series=series />
	</#list>
</ul>
