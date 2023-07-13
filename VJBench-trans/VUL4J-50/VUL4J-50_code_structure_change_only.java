	private void writeSession(SessionInformations session, boolean displayUser) throws IOException {
		final String sessionId = session.getId();
		final String remoteAddr = session.getRemoteAddr();
		write("<td><a href='?part=sessions&amp;sessionId=");
		String sessionIdHtmlEncode=htmlEncodeButNotSpace(sessionId);
		write(sessionIdHtmlEncode);
		write("'>");
		write(sessionIdHtmlEncode);
		write("</a>");
		final String nextColumnAlignRight = "</td><td align='right'>";
		write(nextColumnAlignRight);
		String lastAccess = durationFormat.format(session.getLastAccess());
		String age = durationFormat.format(session.getAge());
		String exprDate = expiryFormat.format(session.getExpirationDate());
		String attrCount = integerFormat.format(session.getAttributeCount());
		String serializedSize = integerFormat.format(session.getSerializedSize());
		write(lastAccess);
		write(nextColumnAlignRight);
		write(age);
		write(nextColumnAlignRight);
		write(exprDate);
		write(nextColumnAlignRight);
		write(attrCount);
		final String nextColumnAlignCenter = "</td><td align='center'>";
		final String nextColumn = "</td><td>";
		write(nextColumnAlignCenter);
		String s1 = !session.isSerializable()? "<span class='severe'>#non#</span>": "#oui#";
		write(s1);

		write(nextColumnAlignRight);
		write(serializedSize);
		write(nextColumn);
		String s2 = remoteAddr != null? remoteAddr:"&nbsp;";
		write(s2);

		write(nextColumnAlignCenter);
		writeCountry(session);
		final String remoteUser = session.getRemoteUser();
		if (displayUser) {
			write(nextColumn);
			if (remoteUser != null) {
				String remoteUserhtmlEncode = htmlEncodeButNotSpace(remoteUser);
				writeDirectly(remoteUserhtmlEncode);
			} else {
				write("&nbsp;");	
			}
		}

		String sessionIdeEncoded =urlEncode(sessionId);
		write("</td><td align='center' class='noPrint'>");
		write("<a href='?part=sessions");
		write("&amp;action=invalidate_session&amp;sessionId=");
		write(sessionIdeEncoded);
		String str_from_js = getStringForJavascript("confirm_invalidate_session");
		write("' onclick=\"javascript:return confirm('"
				+  str_from_js + "');\">");
		write("<img width='16' height='16' src='?resource=user-trash.png' alt='#invalidate_session#' title='#invalidate_session#' />");
		write("</a>");
		write("</td>");
	}