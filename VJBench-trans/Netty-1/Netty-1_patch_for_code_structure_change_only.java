    private void splitHeader(AppendableCharSequence sb) {
        final int length = sb.length();
        int nameStart;
        int nameEnd;
       
        nameStart = findNonWhitespace(sb, 0);
        nameEnd = nameStart;
        while( nameEnd < length) {
            char ch = sb.charAtUnsafe(nameEnd);
            if (ch != ':' && ! (!isDecodingRequest() && Character.isWhitespace(ch))) {
                nameEnd ++;
            }else{
                break;
            }
           
        }

        int colonEnd;
        colonEnd = nameEnd;
        while ( colonEnd < length) {
            if (sb.charAtUnsafe(colonEnd) != ':') {
                colonEnd ++;
                
            }else{
                colonEnd ++;
                break;
            }
            
        }

        int valueStart;
        int valueEnd;
        name = sb.subStringUnsafe(nameStart, nameEnd);
        valueStart = findNonWhitespace(sb, colonEnd);
       if (valueStart != length) {
            valueEnd = findEndOfString(sb);
            value = sb.subStringUnsafe(valueStart, valueEnd);
        } else{
            value = EMPTY_VALUE;
        }
    }
