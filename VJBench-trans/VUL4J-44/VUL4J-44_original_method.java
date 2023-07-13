public String encodeCharacter( char[] immune, Character c )
{
	String cStr = String.valueOf(c.charValue());
	byte[] bytes;
	StringBuilder sb;
	if(UNENCODED_SET.contains(c))
		return cStr;
	bytes = toUtf8Bytes(cStr);
	sb = new StringBuilder(bytes.length * 3);
	for(byte b : bytes)
		appendTwoUpperHex(sb.append('%'), b);
	return sb.toString();
}
