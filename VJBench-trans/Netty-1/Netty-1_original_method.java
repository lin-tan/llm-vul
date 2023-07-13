private void splitHeader(AppendableCharSequence sb) {
    final int length = sb.length();
    int nameStart;
    int nameEnd;
    int colonEnd;
    int valueStart;
    int valueEnd;
    nameStart = findNonWhitespace(sb, 0);
    for (nameEnd = nameStart; nameEnd < length; nameEnd ++) {
        char ch = sb.charAtUnsafe(nameEnd);
        if (ch == ':' || Character.isWhitespace(ch)) {
            break;
        }
    }
    for (colonEnd = nameEnd; colonEnd < length; colonEnd ++) {
        if (sb.charAtUnsafe(colonEnd) == ':') {
            colonEnd ++;
            break;
        }
    }
    name = sb.subStringUnsafe(nameStart, nameEnd);
    valueStart = findNonWhitespace(sb, colonEnd);
    if (valueStart == length) {
        value = EMPTY_VALUE;
    } else {
        valueEnd = findEndOfString(sb);
        value = sb.subStringUnsafe(valueStart, valueEnd);
    }
}
