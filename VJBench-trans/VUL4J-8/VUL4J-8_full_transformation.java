@Override
public ByteBuffer encode(final String tile) {
    final CharBuffer charBuffer = CharBuffer.wrap(tile);
    
    int initial_cbr = charBuffer.remaining();
    final CharsetEncoder charEncoder = newEncoder();
    int size = approximateOriginalBufferSize(charEncoder, initial_cbr);
    ByteBuffer result = ByteBuffer.allocate(size);
    CharBuffer cb_temp = null;
    
    while (charBuffer.remaining() > 0) {
        final CoderResult outcome = charEncoder.encode(charBuffer, result, false);

        if (!outcome.isUnmappable() && !outcome.isMalformed()) {

            if (outcome.isOverflow()) {
                int cbr = charBuffer.remaining();
                result = CompressedFileEncodingAssistant.enlargeBufferWith(result, approximateAdditionalEncodeingSize(charEncoder, cbr));
            } 
        }else{
            int l = 6 * outcome.length();
            int roomForSubstitute = approximateAdditionalEncodeingSize(charEncoder,l );
            if (roomForSubstitute > result.remaining()) {
    
                int charAmount = 0;
                int j = charBuffer.position() ;
                while ( j < charBuffer.limit() ) {
                    if(charEncoder.ableToEncode(charBuffer.get(j))){
                        charAmount +=1;
                    }else{
                        charAmount +=6;
                    }
                    
                    j++;
                }
                int outRemaining = result.remaining();
                result = CompressedFileEncodingAssistant.enlargeBufferWith(result, approximateAdditionalEncodeingSize(charEncoder, charAmount) - outRemaining);
            }
            
            cb_temp = cb_temp == null? CharBuffer.allocate(6):cb_temp;
    
            int j = 0;
            while ( j < outcome.length()) {
                CharBuffer encodeSubstituteCharBuffer = encodeSubstitute(cb_temp, charBuffer.get());
                result = encodeEntirely(charEncoder, encodeSubstituteCharBuffer , result);
                ++j;
            }
        }  
    }

    charEncoder.encode(charBuffer, result, true);
    result.limit(result.position());
    result.rewind();
    return result;
}