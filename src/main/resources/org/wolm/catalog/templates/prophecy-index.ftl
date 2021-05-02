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
		margin-bottom: 8px;
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
		
		<p class="clear">&nbsp;</p>
	</div>
</#macro>

<#-- -------------------------------------------------------------------------------------- -->

<h1>${title!}</h1>

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

<p />

<div class="prophecyList">
	<#list prophecyList as prophecy>
		<@prophecyItem prophecy=prophecy />
	</#list>
</div>

