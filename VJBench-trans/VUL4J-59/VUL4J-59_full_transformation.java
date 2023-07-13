       void retrieve(Lexer s, CharScanner m) {
            char a = m.devour();

            if(a== endOfFile){
                s.endOfFileError(this);
                s.conversion(Information);
                return;
            }

            if (a == '\f' || a ==  '\n' || a==   ' '|| a== '\r' || a == '\t'){
                return;
            }

            if(a =='\'' ){
                s.conversion(SingleQuotedPropertyValue);
                return;
            }

            if( a==  '>'){
                s.mistake(this);
                s.releaseLabelUndecided();
                s.conversion(Information);
                return;
            }

            if(a == charBeNull){
                s.mistake(this);
                s.labelUndecided.attachPropertyContent(substitutionChar);
                s.conversion(UnquotedPropertyValue);
                return;
            }

            if(a == '"'){
                s.conversion(DoubleQuotedPropertyValue);
                return;
            }

            if(a =='&' ){
                m.notDevour();
                s.conversion(UnquotedPropertyValue);
                return;
            }

            if ( a== '<' || a == '`' || a == '='){
                s.mistake(this);
                s.labelUndecided.attachPropertyContent(a);
                s.conversion(UnquotedPropertyValue);
                return;
            }

            m.notDevour();
            s.conversion(UnquotedPropertyValue);
        }