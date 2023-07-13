  private void sanitizeString(int start, int end) {
    boolean closed = false;
    int i = start;
    while ( i < end) {
      char ch = jsonish.charAt(i);
        if(ch== '\u2028'){ replace(i, i + 1, "\\u2028"); }
        else if( ch=='"'|| ch== '\''){
          if (i != start) {
            if (i + 1 == end) {
              char startDelim = jsonish.charAt(start);
              if (startDelim != '\'') {
                startDelim = '"';
              }
              closed = startDelim == ch;
            }
            
            if(!closed){
              if(ch == '"'){
                insert(i, '\\');
              }
            }else{
               if (ch == '\'') { replace(i, i + 1, '"'); }
            }
           
          } else {
            if (ch == '\'') { replace(i, i + 1, '"'); }
          }
        }
        else if(ch== '/'){
          if (i > start && i + 2 < end && '<' == jsonish.charAt(i - 1)
              && 's' == (jsonish.charAt(i + 1) | 32)
              && 'c' == (jsonish.charAt(i + 2) | 32)) {
            insert(i, '\\');
          }
        }
        else if(ch=='\r'){ replace(i, i + 1, "\\r");}
        else if(ch=='\u2029'){ replace(i, i + 1, "\\u2029"); }
        else if(ch== '\\'){
          if (i + 1 != end) {
            char sch = jsonish.charAt(i + 1);
            if( sch =='b'||sch == 'f'|| sch == 'n'||sch =='r'||sch == 't'||sch == '\\'||
            sch == '/'|| sch == '"'){
              ++i;
            }
            else if( sch == 'v'){  
              replace(i, i + 2, "\\u0008");
              ++i;
            }
            else if( sch == 'x'){
              if (i + 4 >= end || !isHexAt(i+2) || !isHexAt(i+3)) {
                elide(i, i + 1);
              }else{
                replace(i, i + 2, "\\u00");  
                i += 3;
              }
            }
            else if( sch == 'u'){
              if (i + 6 >= end || !isHexAt(i + 2) || !isHexAt(i + 3)
                  || !isHexAt(i + 4) || !isHexAt(i + 5)) {
                elide(i, i + 1);
              }else{
                i += 5;
              }
            }
            else if( sch == '0'|| sch == '1'|| sch == '2'|| sch == '3'||
            sch == '4'||sch == '5'|| sch == '6'|| sch == '7'){
              int octalEnd = i + 1;
              if (octalEnd + 1 < end && isOctAt(octalEnd + 1)) {
                ++octalEnd;
                if (ch <= '3' && octalEnd + 1 < end && isOctAt(octalEnd + 1)) {
                  ++octalEnd;
                }
                int value = 0;
                int j = i;
                while ( j < octalEnd) {
                  value = (value << 3) | (jsonish.charAt(j) - '0');
                  j++;
                }
                replace(i + 1, octalEnd, "u00");
                appendHex(value, 2);
              }
              i = octalEnd - 1;
            }else{
              elide(i, i + 1);
            }
          }else{
             elide(i, i + 1);
            i++;
            continue;
          }
        
          
        } else if(ch==  ']'){
          if (i + 2 < end && ']' == jsonish.charAt(i + 1)
              && '>' == jsonish.charAt(i + 2)) {
            replace(i, i + 1, "\\u005d");
          }
        }else if(ch== '\n'){ 
          replace(i, i + 1, "\\n");
        }
        else{
          if (ch >= 0x20) {
            if (ch >= 0xd800) {  
              if (ch >= 0xe000) { 
                  if (ch <= 0xfffd) { 
                        i++;
                        continue;
                      }
              } else{
                if (Character.isHighSurrogate(ch) && i+1 < end
                    && Character.isLowSurrogate(jsonish.charAt(i+1))) {
                  ++i; 
                  i++;
                  continue;
                }
              }

              
            } else{
              i++;
              continue;

            }
            
          }else{
            if (ch == 9 || ch == 0xa || ch == 0xd) { i++;continue; }
          }
          
          replace(i, i + 1, "\\u");
          int j = 4;
          while ( --j >= 0) {
            sanitizedJson.append(HEX_DIGITS[(ch >>> (j << 2)) & 0xf]);
          }
        }
      
      i++;
    }
    if (closed) {
      return;
    }else{ 
      insert(end, '"'); 
    }
  }