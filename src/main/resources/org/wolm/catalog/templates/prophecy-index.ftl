<#assign defaultColor = '#5a9e5d' />
<#assign highlightColor = '#337e37' />

<style>
	a { color: ${defaultColor}; }
	a:hover { color: ${highlightColor}; }

	span.inprogress { color: #999; font-style: italic; }
	
	.prophecyItem {
		border: 1px ${defaultColor} solid;
		border-radius: 4px;
		height: 152px;
		padding: 4px;
		margin-bottom: 4px;
	}
	.prophecyItem .title {
		font-size: 20px;
	}
	.prophecyItem span.label {
		display: inline-block;
		width: 80px;
		text-align: left;
		vertical-align: top;
	}
	.prophecyItem span.longtext {
		display: inline-block;
		overflow: hidden;
		/*width: 432px;*/
		height: 60px;
		font-size: 10px;
	}
	.prophecyItem p.clear { clear: both; }
</style>

<#macro prophecyItem prophecy>
	<div class="prophecyItem" data-date="${prophecy.date?date?iso_utc}" data-title="${prophecy.title}">
		<p>
			<a class="title" href="${baseRef}/prophecy-${prophecy.id}.html">${prophecy.title?html}</a>
			<br/>
			<span class="label">Location:</span> ${prophecy.location}
			<br/>
			<#if prophecy.date??>
				<span class="label">Date:</span> ${prophecy.date?date}
			<#else>
				&nbsp; 
			</#if>
			<br/>
			<#if prophecy.by??>
				<span class="label">Given By:</span> ${prophecy.by}
			<#else>
				&nbsp; 
			</#if>
			<br/>
			<span class="longtext" title="${prophecy.title?html}">
				<#assign body = prophecy.body />
				<#if body?length gt 800>${body?substring(0,800)}<#else>${body}</#if>
			</span>
		</p>
		
		<span class="filterKey" style="display:none;"><#list prophecy.keywords.keywordList as k>${k} </#list></span>

		<p class="clear">&nbsp;</p>
	</div>
</#macro>

<#-- -------------------------------------------------------------------------------------- -->

<h1>${title!}</h1>

<#-- sorting options -->
<div style="float:right;">
	&nbsp;&nbsp;&nbsp;
	Sort by: 
	<select class="sortingSelect">
		<option value="sortByTitle">Title</option>
		<option value="sortByDateAsc">Date (Oldest First)</option>
		<option value="sortByDateDesc" selected="selected">Date (Newest First)</option>
	</select>
</div>
<#-- filter options -->
<form action="javascript:noop();" class="filterForm" style="float:left;">
	Search: <input type="text" class="filterInput" title="Enter words to search for."/>
</form>
<div style="clear:right"/>
<p/>
<div class="prophecyList">
	<#list prophecyList as prophecy>
		<@prophecyItem prophecy=prophecy />
	</#list>
</div>

<script type="text/javascript">
//<![CDATA[
	/* Given a comparator, will sort the items in the "class=prophecyList" with that comparator
	 * @param comparator One of: sortByDateAsc, sortByDateDesc, sortByTitle  
	 */
	function sortIndex(comparator) {
		switch (comparator) {
		case "sortByDateAsc":
			jQuery('.prophecyList div.prophecyItem').sort(sortByDateAsc).appendTo('.prophecyList');
			break;
		case "sortByDateDesc":
			jQuery('.prophecyList div.prophecyItem').sort(sortByDateDesc).appendTo('.prophecyList');
			break;
		case "sortByTitle":
			jQuery('.prophecyList div.prophecyItem').sort(sortByTitle).appendTo('.prophecyList');
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
			jQuery('.prophecyList').find("span.filterKey:not(:containsIgnoreCase(" + filter + "))").parent().slideUp();
			jQuery('.prophecyList').find("span.filterKey:containsIgnoreCase(" + filter + ")").parent().slideDown();
		} else {
			jQuery('.prophecyList').find("div").slideDown();
		}
	});
    // fire the above change event after every letter
	jQuery('.filterInput').keyup( function () { jQuery(this).change(); });
//]]>
</script>

