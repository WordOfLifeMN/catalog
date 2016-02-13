<#if department! == 'CORE'>
	<#assign defaultColor = '#840' />
	<#assign highlightColor = '#5b2d00' />
<#else>
	<#assign defaultColor = '#5a9e5d' />
	<#assign highlightColor = '#337e37' />
</#if>

<style>
	a { color: ${defaultColor}; }
	a:hover { color: ${highlightColor}; }
	span.inprogress { color: #999; font-style: italic; }
</style>

<#macro seriesSummaryItem series>
	<li data-date="${series.startDate?date?iso_utc}" data-title="${series.titleSortKey}">
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
		<span class="filterKey" style="display:none;"><#list series.keywords.keywordList as k>${k} </#list><span>
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

<#-- sorting options -->
<div style="float:right;">
	&nbsp;&nbsp;&nbsp;
	Sort by: 
	<select class="sortingSelect">
		<option value="sortByTitle">Title</option>
		<option value="sortByDateAsc">Date (Oldest First)</option>
		<option value="sortByDateDesc">Date (Newest First)</option>
	</select>
</div>
<#-- filter options -->
<form action="javascript:noop();" class="filterForm" style="float:left;">
	Filter on: <input type="text" class="filterInput" title="Enter words to search titles and speaker names for. Will search all messages in the series."/>
</form>
<div style="clear:right"/>
<p/>
<ul class="seriesList">
	<#list seriesList as series>
		<@seriesSummaryItem series=series />
	</#list>
</ul>

<script type="text/javascript">
//<![CDATA[
	/* Given a comparator, will sort the items in the "class=seriesList" with that comparator
	* @param comparator One of: sortByDateAsc, sortByDateDesc, sortByTitle  
	*/
	function sortIndex(comparator) {
		switch (comparator) {
		case "sortByDateAsc":
			jQuery('.seriesList li').sort(sortByDateAsc).appendTo('.seriesList');
			break;
		case "sortByDateDesc":
			jQuery('.seriesList li').sort(sortByDateDesc).appendTo('.seriesList');
			break;
		case "sortByTitle":
			jQuery('.seriesList li').sort(sortByTitle).appendTo('.seriesList');
			break;
		}
	}
	function sortByDateAsc(a, b) {
	    return (jQuery(b).data('date')) < (jQuery(a).data('date')) ? 1 : -1;    
	}
	function sortByDateDesc(a, b) {
	    return (jQuery(b).data('date')) > (jQuery(a).data('date')) ? 1 : -1;    
	}
	function sortByTitle(a, b) {
	    return (jQuery(b).data('title')) < (jQuery(a).data('title')) ? 1 : -1;    
	}

	// attach the sorting operation to the sorting menu	
	jQuery('.sortingSelect').change(function() {
		sortIndex(jQuery(this).val());
	});
	
	/*
	* attach the filtering operation to the filter field
	*/
	function noop() { return; }
	// create a case-insensitive selector
	jQuery.expr[':'].containsIgnoreCase = function(a,i,m){
		return (a.textContent || a.innerText || "").toLowerCase().indexOf(m[3].toLowerCase())>=0;
	};
	// attach the filters to the filter input control
	jQuery('.filterInput').change( function () {
		var filter = jQuery(this).val(); // get the value of the input, which we filter on
		if (filter) {
			jQuery('.seriesList').find("span.filterKey:not(:containsIgnoreCase(" + filter + "))").parent().slideUp();
			jQuery('.seriesList').find("span.filterKey:containsIgnoreCase(" + filter + ")").parent().slideDown();
		} else {
			jQuery('.seriesList').find("li").slideDown();
		}
	});
    // fire the above change event after every letter
	jQuery('.filterInput').keyup( function () { jQuery(this).change(); });
//]]>
</script>

