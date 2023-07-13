@Override
public ByteBuffer encode(final String tile) {
    final CharsetEncoder charEncoder = newEncoder();
    final CharBuffer charBuffer = CharBuffer.wrap(tile);
    CharBuffer cb_temp = null;
    ByteBuffer result = ByteBuffer.allocate(approximateOriginalBufferSize(charEncoder, charBuffer.remaining()));
    while (charBuffer.remaining() > 0) {
        final CoderResult outcome = charEncoder.encode(charBuffer, result, false);
        if (outcome.isUnmappable() || outcome.isMalformed()) {
            int roomForSubstitute = approximateAdditionalEncodeingSize(charEncoder, 6 * outcome.length());
            if (roomForSubstitute > result.remaining()) {
                int charAmount = 0;
                for (int j = charBuffer.position() ; j < charBuffer.limit(); j++) {
                    charAmount += !charEncoder.ableToEncode(charBuffer.get(j)) ? 6 : 1;
                }
                int sumSupernumeraryInfinite = approximateAdditionalEncodeingSize(charEncoder, charAmount);
                result = CompressedFileEncodingAssistant.enlargeBufferWith(result, sumSupernumeraryInfinite - result.remaining());
            }
            if (cb_temp == null) {
                cb_temp = CharBuffer.allocate(6);
            }
            for (int j = 0; j < outcome.length(); ++j) {
                result = encodeEntirely(charEncoder, encodeSubstitute(cb_temp, charBuffer.get()), result);
            }
        } else if (outcome.isOverflow()) {
            int increase = approximateAdditionalEncodeingSize(charEncoder, charBuffer.remaining());
            result = CompressedFileEncodingAssistant.enlargeBufferWith(result, increase);
        }
    }
    charEncoder.encode(charBuffer, result, true);
    result.limit(result.position());
    result.rewind();
    return result;
}