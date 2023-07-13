       void read(Tokeniser t, CharacterReader r) {
            char c = r.consume();

            if(c== eof){
                t.eofError(this);
                t.transition(Data);
                return;
            }

            if (c == '\f' || c ==  '\n' || c==   ' '|| c== '\r' || c == '\t'){
                return;
            }

            if(c =='\'' ){
                t.transition(AttributeValue_singleQuoted);
                return;
            }

            if( c==  '>'){
                t.error(this);
                t.emitTagPending();
                t.transition(Data);
                return;
            }

            if(c == nullChar){
                t.error(this);
                t.tagPending.appendAttributeValue(replacementChar);
                t.transition(AttributeValue_unquoted);
                return;
            }

            if(c == '"'){
                t.transition(AttributeValue_doubleQuoted);
                return;
            }

            if(c =='&' ){
                r.unconsume();
                t.transition(AttributeValue_unquoted);
                return;
            }

            if ( c== '<' || c == '`' || c == '='){
                t.error(this);
                t.tagPending.appendAttributeValue(c);
                t.transition(AttributeValue_unquoted);
                return;
            }

            r.unconsume();
            t.transition(AttributeValue_unquoted);
        }