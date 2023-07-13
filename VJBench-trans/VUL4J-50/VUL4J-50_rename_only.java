private void composeSession(SessionData session, boolean showExploiter) throws IOException {
	final String followingColumnAlignRightField = "</td><td align='right'>";
	final String followingColumnAlignCore = "</td><td align='center'>";
	compose("<td><a href='?part=sessions&amp;sessionId=");
	compose(htmlEnocdeWithoutSpace(session.acquireId()));
	compose("'>");
	compose(htmlEnocdeWithoutSpace(session.acquireId()));
	compose("</a>");
	compose(followingColumnAlignRightField);
	compose(timeSpanFormatting.format(session.acquireFinalAccess()));
	compose(followingColumnAlignRightField);
	compose(timeSpanFormatting.format(session.acquireHistoricPeriod()));
	compose(followingColumnAlignRightField);
	compose(terminationFormatting.format(session.acquireTerminationTime()));
	compose(followingColumnAlignRightField);
	compose(wholeNumberFormatting.format(session.acquirePropertyCounting()));
	compose(followingColumnAlignCore);
	if (session.whetherSerializable()) {
		compose("#oui#");
	} else {
		compose("<span class='severe'>#non#</span>");
	}
	compose(followingColumnAlignRightField);
	compose(wholeNumberFormatting.format(session.acquireSerializedSize()));
	final String followingColumn = "</td><td>";
	compose(followingColumn);
	final String offSiteControlLocation = session.obtainOffSiteLocation();
	if (offSiteControlLocation == null) {
		compose("&nbsp;");
	} else {
		compose(offSiteControlLocation);
	}
	compose(followingColumnAlignCore);
	composeState(session);
	if (showExploiter) {
		compose(followingColumn);
		final String offSiteControlExploiter = session.acquireOffSiteControlExploiter();
		if (offSiteControlExploiter == null) {
			compose("&nbsp;");
		} else {
			composeStraightly(htmlEnocdeWithoutSpace(offSiteControlExploiter));
		}
	}
	compose("</td><td align='center' class='noPrint'>");
	compose(A_HREF_PART_SESSIONS);
	compose("&amp;action=invalidate_session&amp;sessionId=");
	compose(encodeUrl(session.acquireId()));
	compose("' onclick=\"javascript:return confirm('"
			+ acquireJavascriptString("confirm_invalidate_session") + "');\">");
	compose("<img width='16' height='16' src='?resource=user-trash.png' alt='#invalidate_session#' title='#invalidate_session#' />");
	compose("</a>");
	compose("</td>");
}