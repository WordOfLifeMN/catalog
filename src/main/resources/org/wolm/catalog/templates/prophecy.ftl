<#assign defaultColor = '#5a9e5d' />
<#assign highlightColor = '#337e37' />

<style>
	span.label {
		display: inline-block;
		width: 80px;
		text-align: left;
		vertical-align: top;
	}
	div.body {
		font-size: 16px;
	}
</style>

<h1>${prophecy.title}</h1>
<p>
	<#if prophecy.location??>
		<span class="label">Location:</span> ${prophecy.location}
		<br/>
	</#if>
	<span class="label">Date:</span> ${prophecy.date?date}
	<br/>
	<#if prophecy.by??>
		<span class="label">Given By:</span> ${prophecy.by}
		<br/>
	</#if>
</p>
<div class="body">
	${prophecy.htmlBody}
</div>
<p>
	<a href="${baseRef}/prophecies.html">[return to prophecies page]</a>
</p>

