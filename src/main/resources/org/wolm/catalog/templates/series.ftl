<h2>${series.title}</h2>
<table>
	<#-- cover art and description -->
	<tr>
		<#if series.coverArtLink??>
			<td valign="top"><img src="${series.coverArtLink}" width="128"/></td>
		</#if>
		<td valign="top" <#if !series.coverArtLink??>colspan="2"</#if>>
			<b>${series.title}</b><br/>
			${series.description!}
		</td>
	</tr>
	
	<#-- speaker -->
	<#if series.speakers?size &gt; 0>
		<tr>
			<td><#if series.speakers?size == 1>Speaker<#else>Speakers</#if>:</td>
			<td><#list series.speakers as speaker>${speaker}<#if speaker_has_next>, </#if></#list></td>
		</tr>
	</#if>
	
	<#-- dates -->
	<tr>
		<td>
			<#if !(series.startDate??) || !(series.endDate??) || series.endDate?date == series.startDate?date>
				Date:
			<#else>
				Dates:
			</#if>
		</td>
		<td>
			<#if series.startDate??>
				${series.startDate?date}
				<#if series.endDate?? && series.endDate?date != series.startDate?date>
					- ${series.endDate?date}
				</#if>
			</#if>
			(${series.messageCount} <#if series.messageCount == 1>message<#else>messages</#if>)
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
		element.css('border-color','#ffd700');
	}
	function mouseExitMessage(element) {
		element.css('border-color','#3e713f');
	}
//]]>
</script>

<table width="100%">	
	<#-- messages -->
	<#list series.messages as message>
		<tr>
			<td onmouseover="mouseEnterMessage(jQuery(this));" onmouseout="mouseExitMessage(jQuery(this));" 
					style="border-style:solid;border-width:1px;border-color:#3e713f;">
				<div onclick="togglePlayer(jQuery(this).parent());" style="display:block;padding:1px;" title="${message.description!}">
					${message_index + 1}.
					<b>${message.title}</b>
					- <#list message.speakers as speaker>${speaker}<#if speaker_has_next>, </#if></#list>
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
										<img src="https://s3-us-west-2.amazonaws.com/wordoflife.mn.audio/etc/YouTubeIcon.jpg" />
									</a>
								</td>
							</#if>
						</tr>
					</table>
				</div>
			</td>
		</tr>
	</#list>
</table>