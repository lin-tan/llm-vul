public String convertChar( char[] exempt, Character a )
{
	String charString = String.valueOf(a.charValue());
	byte[] byteArray;
	StringBuilder x;
	if(NOT_ENCODE_COLLECTION.contains(a))
		return charString;
	byteArray = convertToUtf8Bytes(charString);
	x = new StringBuilder(byteArray.length * 3);
	for(byte y : byteArray)
		attachTwoUppercaseHexadecimal(x.append('%'), y);
	return x.toString();
}