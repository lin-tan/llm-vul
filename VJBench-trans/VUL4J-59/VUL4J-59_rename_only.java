void retrieve(Lexer s, CharScanner m) {
    char a = m.devour();
    switch (a) {
        case '\t':
        case '\n':
        case '\r':
        case '\f':
        case ' ':
            break;
        case '"':
            s.conversion(DoubleQuotedPropertyValue);
            break;
        case '&':
            m.notDevour();
            s.conversion(UnquotedPropertyValue);
            break;
        case '\'':
            s.conversion(SingleQuotedPropertyValue);
            break;
        case charBeNull:
            s.mistake(this);
            s.labelUndecided.attachPropertyContent(substitutionChar);
            s.conversion(UnquotedPropertyValue);
            break;
        case endOfFile:
            s.endOfFileError(this);
            s.conversion(Information);
            break;
        case '>':
            s.mistake(this);
            s.releaseLabelUndecided();
            s.conversion(Information);
            break;
        case '<':
        case '=':
        case '`':
            s.mistake(this);
            s.labelUndecided.attachPropertyContent(a);
            s.conversion(UnquotedPropertyValue);
            break;
        default:
            m.notDevour();
            s.conversion(UnquotedPropertyValue);
    }
}