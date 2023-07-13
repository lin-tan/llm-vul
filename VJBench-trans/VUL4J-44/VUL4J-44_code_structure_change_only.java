	public String encodeCharacter( char[] immune, Character c )
	{
		char charValue = c.charValue();
		String cStr = String.valueOf(charValue);
		if(!UNENCODED_SET.contains(c)){
			byte[] bytes = toUtf8Bytes(cStr);
			StringBuilder sb = new StringBuilder(bytes.length * 3);
			int i =0;
			while(i<bytes.length){
				sb.append('%');
				appendTwoUpperHex(sb, bytes[i]);
				i++;
			}
			return sb.toString();
		}
		
		return cStr;

	}
