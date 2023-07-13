@Override
public ByteBuffer encode(final String name) {
    final CharBuffer cb = CharBuffer.wrap(name);
    
    int initial_cbr = cb.remaining();
    final CharsetEncoder enc = newEncoder();
    int size = estimateInitialBufferSize(enc, initial_cbr);
    ByteBuffer out = ByteBuffer.allocate(size);
    CharBuffer tmp = null;
    
    while (cb.remaining() > 0) {
        final CoderResult res = enc.encode(cb, out, false);

        if (!res.isUnmappable() && !res.isMalformed()) {

            if (res.isOverflow()) {
                int cbr = cb.remaining();
                out = ZipEncodingHelper.growBufferBy(out, estimateIncrementalEncodingSize(enc, cbr));
            } 
        }else{
            int l = 6 * res.length();
            int spaceForSurrogate = estimateIncrementalEncodingSize(enc,l );
            if (spaceForSurrogate > out.remaining()) {
    
                int charCount = 0;
                int i = cb.position() ;
                while ( i < cb.limit() ) {
                    if(enc.canEncode(cb.get(i))){
                        charCount +=1;
                    }else{
                        charCount +=6;
                    }
                    
                    i++;
                }
                int outRemaining = out.remaining();
                out = ZipEncodingHelper.growBufferBy(out, estimateIncrementalEncodingSize(enc, charCount) - outRemaining);
            }
            
            tmp = tmp == null? CharBuffer.allocate(6):tmp;
    
            int i = 0;
            while ( i < res.length()) {
                CharBuffer encodeSurrogateCharBuffer = encodeSurrogate(tmp, cb.get());
                out = encodeFully(enc, encodeSurrogateCharBuffer, out);
                ++i;
            }
        }  
    }

    enc.encode(cb, out, true);
    out.limit(out.position());
    out.rewind();
    return out;
}
