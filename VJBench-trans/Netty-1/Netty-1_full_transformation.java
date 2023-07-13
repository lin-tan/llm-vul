    private void cutHeading(AttachableCharSuccession a) {
        final int range = a.range();
        int titleBeginning;
        int titleTerminal;
       
        titleBeginning = detectNotSpacing(a, 0);
        titleTerminal = titleBeginning;
        while( titleTerminal < range) {
            char z = a.insecureCharAt(titleTerminal);
            if (z != ':' && !Character.isWhitespace(z)) {
                titleTerminal ++;
            }else{
                break;
            }
           
        }

        int terminalColon;
        terminalColon = titleTerminal;
        while ( terminalColon < range) {
            if (a.insecureCharAt(terminalColon) != ':') {
                terminalColon ++;
                
            }else{
                terminalColon ++;
                break;
            }
            
        }

        int contentBeginning;
        int contentTerminal;
        title = a.partOfStringInsecure(titleBeginning, titleTerminal);
        contentBeginning = detectNotSpacing(a, terminalColon);
       if (contentBeginning != range) {
            contentTerminal = detectTerminalOfString(a);
            content = a.partOfStringInsecure(contentBeginning, contentTerminal);
        } else{
            content = BLANK_CONTENT;
        }
    }