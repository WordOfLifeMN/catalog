<h1>${series.title}</h1>
<table>
	<#-- cover art and description -->
	<#assign artLink = series.coverArtLink!'https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/WordofLifeLogo-XSmall.png' />
	<tr>
		<#if artLink??>
			<td valign="top"><img src="${artLink}" width="128"/></td>
		</#if>
		<td valign="top" <#if !series.coverArtLink??>colspan="2"</#if>>
			<b>${series.title}</b>
			
			<#if series.speakers?size &gt; 0 || series.StartDate??><br/></#if>

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
			
			<br/>
			${series.description!}
		</td>
	</tr>
	
</table>

<script type="text/javascript">
//<![CDATA[
	function togglePlayer(element) {
		jQuery('.player').not(element.children('.player')).hide('puff');
		element.children('.player').toggle('puff');
	}
	function mouseEnterMessage(element) {
		element.addClass('highlight');
	}
	function mouseExitMessage(element) {
		element.removeClass('highlight');
	}
//]]>
</script>

<style>
	td.message {
		border: 2px solid #528d54;
		border-radius: 5px;
	}
	td.message div.title {
		padding:1px;
	}
	td.message div.title.highlight {	
		border: 0px solid;
		border-radius: 3px;
		background-color: #3e713f;
		color: white;	
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
	td.resources .source {
		color: #777;
		font-size: 85%;
	}
</style>

<table width="100%">	
	<#-- messages -->
	<#list series.messages as message>
		<tr>
			<td class="message">
				<div class="title" title="${message.description!}"
						onclick="togglePlayer(jQuery(this).parent());"
						onmouseover="mouseEnterMessage(jQuery(this));" onmouseout="mouseExitMessage(jQuery(this));">
					${message_index + 1}.
					<b>${message.title}</b>
					<#if message.speakers??>
						- <#list message.speakers as speaker>${speaker}<#if speaker_has_next>, </#if></#list>
					</#if>
					<#if message.date??>(${message.date?date?string.full})</#if>
				</div>
				<div class="player" style="display:none;">
					<p>${message.description!}</p>
					<table width="100%">
						<tr>
							<#if message.videoLink??>
								<td width="50px" valign="top" align="left">
									<a href="${message.videoLink}" target="wolmVideo">
										<img src="https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/YouTubeIcon.jpg" height="24"/>
									</a>
								</td>
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
							<#assign audioDownloadLink = message.audioLinkForDownload! />
							<#if audioDownloadLink?has_content>
								<td width="88px" valign="top" align="right">
									<a href="${audioDownloadLink}">
										<img src="https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/download.png" height="24px"/>
									</a>
								</td>
							</#if>
						</tr>
					</table>
					<#if message.resources?has_content>
						<div class="message-resource">
							<#list message.resources as resource>
								<#if resource.fileName??>
									<span class="filename">(${resource.fileName!})</span>
								</#if>
								<a href="${resource.link}" target="wolmGuide" style="padding-left:4px;">${resource.name}</a>
								<span style="float:clear;" />
							</#list>
						</div>
					</#if>
				</div>
			</td>
		</tr>
	</#list>
	<#if series.resources?has_content>
		<tr>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="resources">
				<b>Additional Resources</b>
				<#list series.resources as resource>
					<br/>
					<#if resource.fileName??>
						<span class="filename">(${resource.fileName!})</span>
					</#if>
					<a href="${resource.link}" target="wolmGuide">${resource.name}</a>
					<#if resource.sourceMessage??>
						<span class="source">
							from 
							<#if (resource.sourceMessage.getTrackNumber(series.title))??>
								#${resource.sourceMessage.getTrackNumber(series.title)}
							</#if> 
							<i>${resource.sourceMessage.title}</i>
						</span>
					</#if>
					<span style="float:clear;" />
				</#list>
			</td>
		</tr>
	</#if>
	
</table>