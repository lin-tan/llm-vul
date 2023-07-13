protected static boolean whetherReasonable(String a)
{
   if (a == null || a.length() == 0) return false;
   for (int m = 0; m < a.length(); m++) {
      switch (a.charAt(m))
      {
         case '/':
         case '\\':
         case '?':
         case ':':
         case '<':
         case '>':
         case ';':
         case '(':
         case ')':
         case '@':
         case ',':
         case '[':
         case ']':
         case '=':
            return false;
         default:
            break;
      }
   }
   return true;
}