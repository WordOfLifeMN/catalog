<#switch ministry>
	<#case 'TBO'>
		<#assign defaultCover = 'https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/TBOLogo-Small.png' />
		<#assign defaultColor = '#424242' />
		<#assign highlightColor = '#d15541' />
		<#assign titleBackgroundColor = '#222222' />
		<#break>
	<#case 'CORE'>
		<#assign defaultCover = 'https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/CORELogo-Small.jpg' />
		<#assign defaultColor = '#840' />
		<#assign highlightColor = '#5b2d00' />
		<#assign titleBackgroundColor = '#452209' />
		<#break>
	<#default>
		<#assign defaultCover = 'https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/WordofLifeLogo-XSmall.png' />
		<#assign defaultColor = '#5a9e5d' />
		<#assign highlightColor = '#337e37' />
		<#assign titleBackgroundColor = '#3e713f' />
		<#break>
</#switch>

<style>
	span.inprogress { color: #999; font-style: italic; }
	
	div.message {
		border: 2px solid #528d54;
		border-radius: 5px;
		margin-bottom: 8px;
	}
	div.message div.title {
		padding:1px;
		border: 0px solid;
		border-radius: 3px;
		background-color: ${titleBackgroundColor};
		color: white;	
	}
	div.message { 
		border: 2px solid ${defaultColor}; 
	}
	div.message div.title.highlight { 
		background-color: ${highlightColor}; 
	}

	div.message.CORE { 
		border: 2px solid #840; 
	}
	div.message.CORE div.title { 
		background-color: #452209;
	}
	div.message.CORE div.title.highlight { 
		background-color: #5b2d00; 
	}

	td.resources {
		border: 2px solid #bf9c03;
		border-radius: 5px; 
	}
	td.resources .filename, div.message-resource .filename {
		color: #bf9c03;
		float: right;
		font-size: 75%;
	}
	td.resources a {
		padding-left: 24px;
	}
	div.message-resource {
		padding-bottom: 4px;
	}
	div.message-resource .source {
		color: #777;
		font-size: 75%;
	}
	div.message-resource .title {
		padding-top: 12px;
	}
	td.seriesHeader {
		padding-left: 15px;
	}
</style>


<h1>${series.title}</h1>
<p/>
<table>
	<#-- cover art and description -->
	<#assign artLink = series.coverArtLink!defaultCover />
	<tr>
		<#if artLink??>
			<td valign="top"><img src="${artLink}" width="128" alt="Series cover"/></td>
		</#if>
		<td class="seriesHeader" valign="top" <#if !series.coverArtLink??>colspan="2"</#if>>
			<p>
				<!-- 
					<b>${series.title}</b>
					<#if series.speakers?size &gt; 0 || series.StartDate??><br/></#if>
				-->
	
				<#-- speaker -->
				<#if series.speakers?size &gt; 0>
					<#list series.speakers as speaker>${speaker}<#if speaker_has_next>, </#if></#list>
				</#if>
				
				<#if (series.speakers?size &gt; 0) && (series.startDate??)>/</#if>
	
				<#-- dates -->
				<#if series.startDate??>
					<#if !(series.endDate??)>Started</#if> <#-- still in progress -->
					${series.startDate?date}
					<#if series.endDate?? && series.endDate?date != series.startDate?date>
						- ${series.endDate?date}
					</#if>
				</#if>
				(${series.messageCount} <#if series.messageCount == 1>message<#else>messages</#if>)
			</p>
			
			<p>${series.description!}<p>
		</td>
	</tr>
	
</table>

<div>	
	<#-- messages -->
	<#list series.filteredMessages as message>
		<div class="message ${message.ministry}">
			<div class="title" title="${message.description!}">
				${message_index + 1}.
				<b>${message.title}</b>
				<#if message.speakers??>
					- <#list message.speakers as speaker>${speaker}<#if speaker_has_next>, </#if></#list>
				</#if>
				<#if message.date??>(${message.date?date?string.full})</#if>
			</div>
			<div class="player">
				<p>${message.description!}</p>
				<table width="100%">
					<tr>
						<#if message.videoLink??>
							<td width="70px" valign="middle" align="center">
								<a href="${message.videoLink}" target="wolmVideo">
									<img src="https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/YouTubeIcon.jpg" height="24" alt="YouTube"/>
								</a>
							</td>
						<#else>
							<td width="15px">&nbsp;</td>
						</#if>
						<td valign="top">
							<#if message.audioLink??>
								<audio controls style="width:100%;">
									<source src="${message.audioLink}" type="audio/mpeg" />
								</audio>
							<#else>
								no audio is available for this message
							</#if>
						</td>
						<td width="15px">&nbsp;</td>
					</tr>
				</table>
				<#if message.resources?has_content>
					<div class="message-resource">
						<#list message.resources as resource>
							<div>
								<#if resource.fileName??>
									<span class="filename">(${resource.fileName!})</span>
								</#if>
								<a href="${resource.link}" target="wolmGuide" style="padding-left:4px;">${resource.name}</a>
								<span style="float:clear;" />
							</div>
						</#list>
					</div>
				</#if>
			</div>
		</div>
	</#list>
	
	<#if series.startDate?? && !(series.endDate??)>
		<div>
			<p><span class="inprogress">- there is still more to come in this series!</span></p>
		</div>
	</#if>
	
	<#if series.resources?has_content>
		<div class="message-resource">
			<p class="title">
				<b><u>Booklets &amp; Resources</u></b>
			</p>
			<#list series.resources as resource>
				<div>
					<#if resource.fileName??>
						<span class="filename">(${resource.fileName!})</span>
					</#if>
					<a href="${resource.link}" target="wolmGuide" style="padding-left:4px;">${resource.name}</a>
					<span style="float:clear;" />
					<#if resource.sourceMessage??>
						<span class="source">
							from 
							<#if (resource.sourceMessage.getTrackNumber(series.title))??>
								#${resource.sourceMessage.getTrackNumber(series.title)}
							</#if> 
							<i>${resource.sourceMessage.title}</i>
						</span>
					</#if>
				</div>
			</#list>
		</div>
	</#if>
</div>
