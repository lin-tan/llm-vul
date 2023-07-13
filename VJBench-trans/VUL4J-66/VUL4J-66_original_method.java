protected static boolean isValid(String str)
{
   if (str == null || str.length() == 0) return false;
   for (int i = 0; i < str.length(); i++) {
      switch (str.charAt(i))
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
