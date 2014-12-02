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
					<#if message.date??>(${message.date?date})</#if>
				</div>
				<div class="player" style="display:none;">
					<p>${message.description!}</p>
					<table width="100%">
						<tr>
							<td width="60%" valign="top">
								<#if message.audioLink??>
									<audio controls style="width:100%;">
										<source src="${message.audioLink}" type="audio/mpeg" />
									</audio>
								<#else>
									no audio is available for this message
								</#if>
							</td>
							<#if message.videoLink??>
								<td width="20%" valign="top" align="right">
									<a href="${message.videoLink}" target="wolmVideo">
										<img src="https://s3-us-west-2.amazonaws.com/wordoflife.mn.catalog/YouTubeIcon.jpg" />
									</a>
								</td>
							</#if>
						</tr>
					</table>
					<#if message.resources?has_content>
						<div class="message-resource">
							<#list message.resources as resource>
								<span class="filename" style="float:right;">(${resource.link?replace('.*/','','r')})</span>
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
						<span class="filename" style="float:right;">(${resource.link?replace('.*/','','r')})</span>
						<a href="${resource.link}" target="wolmGuide" style="padding-left:24px;">${resource.name}</a>
						<span style="float:clear;" />
					</#list>
			</td>
		</tr>
	</#if>
	
</table>