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
		padding: 4px;
		margin-bottom: 8px;
	}

	.seriesItem.CORE a { color: #840; }
	.seriesItem.CORE a:hover { color: #5b2d00; }
	.seriesItem.CORE {
		border: 1px #840 solid;
	}

	.seriesItem .title {
		font-size: 20px;
		font-weight: bold;
	}
	.seriesItem .coverArt {
		float: left;
		margin-right: 3px;
		width: 72px;
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
		/* width: 15%; */
		/* text-align: left; */
		font-weight: bolder;
		vertical-align: top;
	}
	.seriesItem span.text {
		/* width: 82% */
	}
	.seriesItem span.longtext {
		display: inline-block;
		/*white-space: nowrap;*/
		overflow: hidden;
		/* width: 82%; */
		margin-top: 10px;
		height: 3em;
		line-height: 1.0;
		font-size: smaller;
	}
	
	.seriesItem p.clear { 
		clear: both;
		display: none; 
	}
	
	.seriesItem td.coverArt {
		width: 10%;
	}
	.seriesItem td.info {
		width: 90%;
	}
</style>

<#macro seriesSummaryItem series>
	<#assign seriesMinistry = series.messages[0].ministry />
	<div class="seriesItem ${seriesMinistry}" data-date="${series.startDate?date?iso_utc}" data-title="${series.titleSortKey}">
		<table>
			<tr>
				<td class="coverArt">
					<#switch seriesMinistry>
						<#case 'CORE'>
							<#local artLink = series.coverArtLink!'https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/CORELogo-Small.jpg' />
							<#break>
						<#default>
							<#local artLink = series.coverArtLink!defaultCover />
							<#break>
					</#switch>
					<div class="coverArt">
						<a href="${baseRef}/${series.id}.html"><img src="${artLink}" alt="Series cover"/></a>
					</div>
				</td>
				<td class="info">
					<a class="title" href="${baseRef}/${series.id}.html">${series.title}</a>
					<br/>
					<span class="label">Messages:</span> 
					<span class="text">${series.messageCount}</span>
					<br/>
					<span class="label">Date:</span>
					<#if series.startDate??>
						<span class="text">
							<#if !(series.endDate??)>Started</#if> <#-- still in progress -->
							${series.startDate?date}
							<#if series.endDate?? && series.endDate?date != series.startDate?date>
								- ${series.endDate?date}
							</#if>
						</span>
						<#if !(series.endDate??)><span class="inprogress"> -more to come!</span></#if>
					</#if>
					<#if series.speakers?size &gt; 0>
						<br/>
						<span class="label">Speaker<#if series.speakers?size &gt; 1>s</#if>:</span> 
						<span class="text">
							<#list series.speakers as speaker>${speaker}<#if speaker?has_next>, </#if></#list>
						</span>
					</#if>
				</td>
			</tr>
		</table>

		<p class="clear">&nbsp;</p>
		<#if series.description??>
			<!-- <span class="label">Description:</span> --> 
			<span class="longtext" title="${series.description?html}">${series.description}</span>
		</#if>

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
					<img src='https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/corestaff.jpg' width='164' alt='staff photo'/>
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
<div>
	Sort by: 
	<select class="sortingSelect" onchange="javascript:location.href = this.value;">
		<#assign sortAZName = pageBaseName />
		<#assign sort09Name = pageBaseName?replace(".html", "-09.html") />
		<#assign sort90Name = pageBaseName?replace(".html", "-90.html") />
	    <option value="${baseRef}/${sortAZName}" <#if pageName == sortAZName>selected</#if>>Title</option>
	    <option value="${baseRef}/${sort09Name}" <#if pageName == sort09Name>selected</#if>>Date - Oldest first</option>
	    <option value="${baseRef}/${sort90Name}" <#if pageName == sort90Name>selected</#if>>Date - Recent first</option>
	</select>
</div>

<p></p>

<div class="seriesList">
	<#list seriesList as series>
		<@seriesSummaryItem series=series />
	</#list>
</div>
