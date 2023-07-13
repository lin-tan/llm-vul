	private void composeSession(SessionData session, boolean showExploiter) throws IOException {
		final String sessionId = session.acquireId();
		final String offSiteControlLocation = session.obtainOffSiteLocation();
		compose("<td><a href='?part=sessions&amp;sessionId=");
		String sessionIdHtmlEncode=htmlEnocdeWithoutSpace(sessionId);
		compose(sessionIdHtmlEncode);
		compose("'>");
		compose(sessionIdHtmlEncode);
		compose("</a>");
		final String followingColumnAlignRightField = "</td><td align='right'>";
		compose(followingColumnAlignRightField);
		String lastAccess = timeSpanFormatting.format(session.acquireFinalAccess());
		String age = timeSpanFormatting.format(session.acquireHistoricPeriod());
		String exprDate = terminationFormatting.format(session.acquireTerminationTime());
		String attrCount = wholeNumberFormatting.format(session.acquirePropertyCounting());
		String serializedSize = wholeNumberFormatting.format(session.acquireSerializedSize());
		compose(lastAccess);
		compose(followingColumnAlignRightField);
		compose(age);
		compose(followingColumnAlignRightField);
		compose(exprDate);
		compose(followingColumnAlignRightField);
		compose(attrCount);
		final String followingColumnAlignCore = "</td><td align='center'>";
		final String followingColumn = "</td><td>";
		compose(followingColumnAlignCore);
		String s1 = !session.whetherSerializable()? "<span class='severe'>#non#</span>": "#oui#";
		compose(s1);

		compose(followingColumnAlignRightField);
		compose(serializedSize);
		compose(followingColumn);
		String s2 = offSiteControlLocation != null? offSiteControlLocation:"&nbsp;";
		compose(s2);

		compose(followingColumnAlignCore);
		composeState(session);
		final String offSiteControlExploiter = session.acquireOffSiteControlExploiter();
		if (showExploiter) {
			compose(followingColumn);
			if (offSiteControlExploiter != null) {
				String remoteUserhtmlEncode = htmlEnocdeWithoutSpace(offSiteControlExploiter);
				composeStraightly(remoteUserhtmlEncode);
			} else {
				compose("&nbsp;");	
			}
		}

		String sessionIdeEncoded =encodeUrl(sessionId);
		compose("</td><td align='center' class='noPrint'>");
		compose("<a href='?part=sessions");
		compose("&amp;action=invalidate_session&amp;sessionId=");
		compose(sessionIdeEncoded);
		String str_from_js = acquireJavascriptString("confirm_invalidate_session");
		compose("' onclick=\"javascript:return confirm('"
				+  str_from_js + "');\">");
		compose("<img width='16' height='16' src='?resource=user-trash.png' alt='#invalidate_session#' title='#invalidate_session#' />");
		compose("</a>");
		compose("</td>");
	}