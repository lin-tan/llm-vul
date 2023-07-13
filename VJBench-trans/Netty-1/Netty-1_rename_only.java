private void cutHeading(AttachableCharSuccession a) {
    final int range = a.range();
    int titleBeginning;
    int titleTerminal;
    int terminalColon;
    int contentBeginning;
    int contentTerminal;
    titleBeginning = detectNotSpacing(a, 0);
    for (titleTerminal = titleBeginning; titleTerminal < range; titleTerminal ++) {
        char z = a.insecureCharAt(titleTerminal);
        if (z == ':' || Character.isWhitespace(z)) {
            break;
        }
    }
    for (terminalColon = titleTerminal; terminalColon < range; terminalColon ++) {
        if (a.insecureCharAt(terminalColon) == ':') {
            terminalColon ++;
            break;
        }
    }
    title = a.partOfStringInsecure(titleBeginning, titleTerminal);
    contentBeginning = detectNotSpacing(a, terminalColon);
    if (contentBeginning == range) {
        content = BLANK_CONTENT;
    } else {
        contentTerminal = detectTerminalOfString(a);
        content = a.partOfStringInsecure(contentBeginning, contentTerminal);
    }
}