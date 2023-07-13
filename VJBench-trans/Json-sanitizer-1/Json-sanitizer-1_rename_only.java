private void cleanString(int begin, int terminal) {
  boolean shut = false;
  for (int h = begin; h < terminal; ++h) {
    char currentChar = JSONlike.charAt(h);
    switch (currentChar) {
      case '\n': supplant(h, h + 1, "\\n"); break;
      case '\r': supplant(h, h + 1, "\\r"); break;
      case '\u2028': supplant(h, h + 1, "\\u2028"); break;
      case '\u2029': supplant(h, h + 1, "\\u2029"); break;
      case '"': case '\'':
        if (h == begin) {
          if (currentChar == '\'') { supplant(h, h + 1, '"'); }
        } else {
          if (h + 1 == terminal) {
            char beginDelimiter = JSONlike.charAt(begin);
            if (beginDelimiter != '\'') {
              beginDelimiter = '"';
            }
            shut = beginDelimiter == currentChar;
          }
          if (shut) {
            if (currentChar == '\'') { supplant(h, h + 1, '"'); }
          } else if (currentChar == '"') {
            embed(h, '\\');
          }
        }
        break;
      case '/':
        if (h > begin && h + 2 < terminal && '<' == JSONlike.charAt(h - 1)
            && 's' == (JSONlike.charAt(h + 1) | 32)
            && 'c' == (JSONlike.charAt(h + 2) | 32)) {
          embed(h, '\\');
        }
        break;
      case ']':
        if (h + 2 < terminal && ']' == JSONlike.charAt(h + 1)
            && '>' == JSONlike.charAt(h + 2)) {
          supplant(h, h + 1, "\\u005d");
        }
        break;
      case '\\':
        if (h + 1 == terminal) {
          remove(h, h + 1);
          break;
        }
        char nextChar = JSONlike.charAt(h + 1);
        switch (nextChar) {
          case 'b': case 'f': case 'n': case 'r': case 't': case '\\':
          case '/': case '"':
            ++h;
            break;
          case 'v':
            supplant(h, h + 2, "\\u0008");
            ++h;
            break;
          case 'x':
            if (h + 4 < terminal && whetherHexadecimalAt(h+2) && whetherHexadecimalAt(h+3)) {
              supplant(h, h + 2, "\\u00");
              h += 3;
              break;
            }
            remove(h, h + 1);
            break;
          case 'u':
            if (h + 6 < terminal && whetherHexadecimalAt(h + 2) && whetherHexadecimalAt(h + 3)
                && whetherHexadecimalAt(h + 4) && whetherHexadecimalAt(h + 5)) {
              h += 5;
              break;
            }
            remove(h, h + 1);
            break;
          case '0': case '1': case '2': case '3':
          case '4': case '5': case '6': case '7':
            int octalTerminal = h + 1;
            if (octalTerminal + 1 < terminal && whetherOctalAt(octalTerminal + 1)) {
              ++octalTerminal;
              if (currentChar <= '3' && octalTerminal + 1 < terminal && whetherOctalAt(octalTerminal + 1)) {
                ++octalTerminal;
              }
              int content = 0;
              for (int k = h; k < octalTerminal; ++k) {
                content = (content << 3) | (JSONlike.charAt(k) - '0');
              }
              supplant(h + 1, octalTerminal, "u00");
              addOnHexadecimal(content, 2);
            }
            h = octalTerminal - 1;
            break;
          default:
            remove(h, h + 1);
            break;
        }
        break;
      default:
        if (currentChar < 0x20) {
          if (currentChar == 9 || currentChar == 0xa || currentChar == 0xd) { continue; }
        } else if (currentChar < 0xd800) {
          continue;
        } else if (currentChar < 0xe000) {
          if (Character.whetherHighAlternate(currentChar) && h+1 < terminal
              && Character.whetherLowAlternate(JSONlike.charAt(h+1))) {
            ++h;
            continue;
          }
        } else if (currentChar <= 0xfffd) {
          continue;
        }
        supplant(h, h + 1, "\\u");
        for (int k = 4; --k >= 0;) {
          cleanedJSON.append(HEX_NUMBER[(currentChar >>> (k << 2)) & 0xf]);
        }
        break;
    }
  }
  if (!shut) { embed(terminal, '"'); }
}
