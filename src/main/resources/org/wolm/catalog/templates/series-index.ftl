<#switch ministry>
	<#case 'TBO'>
		<#assign defaultCover = 'https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/TBOLogo-Small.png' />
		<#assign defaultColor = '#424242' />
		<#assign highlightColor = '#d15541' />
		<#break>
	<#case 'CORE'>
		<#assign defaultCover = 'https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/CORELogo-Small.jpg' />
		<#assign defaultColor = '#840' />
		<#assign highlightColor = '#5b2d00' />
		<#break>
	<#default>
		<#assign defaultCover = 'https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/WordofLifeLogo-XSmall.png' />
		<#assign defaultColor = '#5a9e5d' />
		<#assign highlightColor = '#337e37' />
		<#break>
</#switch>

<style>
	a { color: ${defaultColor}; }
	a:hover { color: ${highlightColor}; }

	span.inprogress { color: #999; font-style: italic; }
	
	.seriesItem {
		border: 1px ${defaultColor} solid;
		border-radius: 4px;
		height: 128px;
		padding: 4px;
		margin-bottom: 4px;
	}
	.seriesItem .title {
		font-size: 20px;
	}
	.seriesItem .coverArt {
		float: left;
		margin-right: 10px;
		width: 132px;
		height: 128px;
		position: relative;
	}
	.seriesItem div.coverArt img {
		/* scale to fit */
		max-width: 100%;
		max-height: 100%;
		/* center */
		position: absolute;
		top: 50%;
		left: 50%;
		transform: translate(-50%, -50%);
	}
	.seriesItem span.label {
		display: inline-block;
		width: 80px;
		text-align: left;
		vertical-align: top;
	}
	.seriesItem span.longtext {
		display: inline-block;
		/*white-space: nowrap;*/
		overflow: hidden;
		width: 432px;
		height: 30px;
		font-size: 10px;
	}
	.seriesItem p.clear { clear: both; }
</style>

<#macro seriesSummaryItem series>
	<div class="seriesItem" data-date="${series.startDate?date?iso_utc}" data-title="${series.titleSortKey}">
		<#local artLink = series.coverArtLink!defaultCover />
		<div class="coverArt">
			<a class="title" href="${baseRef}/${series.id}.html"><img src="${artLink}" /></a>
		</div>
		<p>
			<a class="title" href="${baseRef}/${series.id}.html">${series.title}</a>
			<br/>
			<span class="label">Messages:</span> ${series.messageCount}
			<br/>
			<span class="label">Presented:</span>
			<#if series.startDate??>
				<#if !(series.endDate??)>Started</#if> <#-- still in progress -->
				${series.startDate?date}
				<#if series.endDate?? && series.endDate?date != series.startDate?date>
					- ${series.endDate?date}
				</#if>
				<#if !(series.endDate??)><span class="inprogress"> -more to come!</span></#if>
			</#if>
			<#if series.speakers?size &gt; 0>
				<br/>
				<span class="label">Speaker<#if series.speakers?size &gt; 1>s</#if>:</span> 
				<#list series.speakers as speaker>${speaker}<#if speaker?has_next>, </#if></#list>
			</#if>
			<#if series.description??>
				<br/>
				<span class="label">Description:</span> 
				<span class="longtext" title="${series.description?html}">${series.description}</span>
			</#if>
		</p>
		
		<#-- (${series.startDate?date?string.iso} - ${series.endDate?date?string.iso}) -->
		<span class="filterKey" style="display:none;"><#list series.keywords.keywordList as k>${k} </#list><span>

		<p class="clear">&nbsp;</p>
	</div>
</#macro>

<#-- -------------------------------------------------------------------------------------- -->

<h1>${title!}</h1>

<#if promoFileName??>
	<div style="border-style:ridge;border-color:#5a9e5d;border-width:medium;padding:4px;margin-bottom:12px;">
		<#include "${promoFileName}"/>
	</div>
</#if>

<#switch ministry>
	<#case 'CORE'>
		<table>
			<tr>
				<td valign="top">
					<img src='https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/corestaff.jpg' width='164'/>
				</td>
				<td>
					<p>C.O.R.E.: Center of Our Relationship Experiences</p>
					<p>
						Mary Peltz is a certified counselor with A.A.C.C. and is a Co-Pastor at Word of Life Ministries 
						which is affiliated and licensed through 
						<a href=\"http://www.afcminternational.org\" target=\"_blank\">A.F.C.M. International</a>.
					</p>
					<p>
						Mary specializes in communication skills and restoring relationships and families. 
						She administrates C.O.R.E. programs which is a \"Freedom From\" program that brings help to 
						schools, group homes and staff situations. She is currently facilitating C.O.R.E. Programs at 
						the jails in the Northern Minnesota areas.
					</p>
				</td>
			</tr>
		</table>
		<#break>
	<#case 'Ask Pastor'>
		<br/>
		<em>Always be prepared to give an answer to everyone who asks you to give the reason for the hope that you have. (1 Peter 3:15)</em>
		<br/>
		<br/>
		Too many times we see things in the world around us or find things in the Word of God that we don't understand. 
		If you have questions about what you see, read, or hear, these short messages might have the answers you are 
		looking for. 
		<br/>
		<br/>
		Pastor Vern fields questions submitted to him from the congregation or anyone online. 
		If you have a question for Pastor Vern, please <a href="mailto:wordoflife.mn@gmail.com">email it to us</a>.
		<br/>
		<br/>
		<#break>
</#switch>
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
<div class="seriesList">
	<#list seriesList as series>
		<@seriesSummaryItem series=series />
	</#list>
</div>

<script type="text/javascript">
//<![CDATA[
	/* Given a comparator, will sort the items in the "class=seriesList" with that comparator
	 * @param comparator One of: sortByDateAsc, sortByDateDesc, sortByTitle  
	 */
	function sortIndex(comparator) {
		switch (comparator) {
		case "sortByDateAsc":
			jQuery('.seriesList div.seriesItem').sort(sortByDateAsc).appendTo('.seriesList');
			break;
		case "sortByDateDesc":
			jQuery('.seriesList div.seriesItem').sort(sortByDateDesc).appendTo('.seriesList');
			break;
		case "sortByTitle":
			jQuery('.seriesList div.seriesItem').sort(sortByTitle).appendTo('.seriesList');
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
			jQuery('.seriesList').find("div").slideDown();
		}
	});
    // fire the above change event after every letter
	jQuery('.filterInput').keyup( function () { jQuery(this).change(); });
//]]>
</script>

