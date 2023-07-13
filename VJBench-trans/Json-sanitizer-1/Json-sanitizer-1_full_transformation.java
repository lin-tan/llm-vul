  private void cleanString(int begin, int terminal) {
    boolean shut = false;
    int h = begin;
    while ( h < terminal) {
      char currentChar = JSONlike.charAt(h);
        if(currentChar== '\u2028'){ supplant(h, h + 1, "\\u2028"); }
        else if( currentChar=='"'|| currentChar== '\''){
          if (h != begin) {
            if (h + 1 == terminal) {
              char beginDelimiter = JSONlike.charAt(begin);
              if (beginDelimiter != '\'') {
                beginDelimiter = '"';
              }
              shut = beginDelimiter == currentChar;
            }
            
            if(!shut){
              if(currentChar == '"'){
                embed(h, '\\');
              }
            }else{
               if (currentChar == '\'') { supplant(h, h + 1, '"'); }
            }
           
          } else {
            if (currentChar == '\'') { supplant(h, h + 1, '"'); }
          }
        }
        else if(currentChar== '/'){
          if (h > begin && h + 2 < terminal && '<' == JSONlike.charAt(h - 1)
              && 's' == (JSONlike.charAt(h + 1) | 32)
              && 'c' == (JSONlike.charAt(h + 2) | 32)) {
            embed(h, '\\');
          }
        }
        else if(currentChar=='\r'){ supplant(h, h + 1, "\\r");}
        else if(currentChar=='\u2029'){ supplant(h, h + 1, "\\u2029"); }
        else if(currentChar== '\\'){
          if (h + 1 != terminal) {
            char nextChar = JSONlike.charAt(h + 1);
            if( nextChar =='b'||nextChar == 'f'|| nextChar == 'n'||nextChar =='r'||nextChar == 't'||nextChar == '\\'||
            nextChar == '/'|| nextChar == '"'){
              ++h;
            }
            else if( nextChar == 'v'){  
              supplant(h, h + 2, "\\u0008");
              ++h;
            }
            else if( nextChar == 'x'){
              if (h + 4 >= terminal || !whetherHexadecimalAt(h+2) || !whetherHexadecimalAt(h+3)) {
                remove(h, h + 1);
              }else{
                supplant(h, h + 2, "\\u00");  
                h += 3;
              }
            }
            else if( nextChar == 'u'){
              if (h + 6 >= terminal || !whetherHexadecimalAt(h + 2) || !whetherHexadecimalAt(h + 3)
                  || !whetherHexadecimalAt(h + 4) || !whetherHexadecimalAt(h + 5)) {
                remove(h, h + 1);
              }else{
                h += 5;
              }
            }
            else if( nextChar == '0'|| nextChar == '1'|| nextChar == '2'|| nextChar == '3'||
            nextChar == '4'||nextChar == '5'|| nextChar == '6'|| nextChar == '7'){
              int octalTerminal = h + 1;
              if (octalTerminal + 1 < terminal && whetherOctalAt(octalTerminal + 1)) {
                ++octalTerminal;
                if (currentChar <= '3' && octalTerminal + 1 < terminal && whetherOctalAt(octalTerminal + 1)) {
                  ++octalTerminal;
                }
                int content = 0;
                int k = h;
                while ( k < octalTerminal) {
                  content = (content << 3) | (JSONlike.charAt(k) - '0');
                  k++;
                }
                supplant(h + 1, octalTerminal, "u00");
                addOnHexadecimal(content, 2);
              }
              h = octalTerminal - 1;
            }else{
              remove(h, h + 1);
            }
          }else{
             remove(h, h + 1);
            h++;
            continue;
          }
        
          
        } else if(currentChar==  ']'){
          if (h + 2 < terminal && ']' == JSONlike.charAt(h + 1)
              && '>' == JSONlike.charAt(h + 2)) {
            supplant(h, h + 1, "\\u005d");
          }
        }else if(currentChar== '\n'){ 
          supplant(h, h + 1, "\\n");
        }
        else{
          if (currentChar >= 0x20) {
            if (currentChar >= 0xd800) {  
              if (currentChar >= 0xe000) { 
                  if (currentChar <= 0xfffd) { 
                        h++;
                        continue;
                      }
              } else{
                if (Character.whetherHighAlternate(currentChar) && h+1 < terminal
                    && Character.whetherLowAlternate(JSONlike.charAt(h+1))) {
                  ++h; 
                  h++;
                  continue;
                }
              }

              
            } else{
              h++;
              continue;

            }
            
          }else{
            if (currentChar == 9 || currentChar == 0xa || currentChar == 0xd) { h++;continue; }
          }
          
          supplant(h, h + 1, "\\u");
          int k = 4;
          while ( --k >= 0) {
            cleanedJSON.append(HEX_NUMBER[(currentChar >>> (k << 2)) & 0xf]);
          }
        }
      
      h++;
    }
    if (shut) {
      return;
    }else{ 
      embed(terminal, '"'); 
    }
  }