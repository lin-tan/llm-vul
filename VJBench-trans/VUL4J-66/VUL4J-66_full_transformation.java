protected static boolean whetherReasonable(String a)
{
   if (a != null && a.length() != 0){
      int m =0;
      while(m < a.length()) {
         char c = a.charAt(m);
         m++;
         if (c == '/'|| c=='\\' || c == '?' || c == ':' || c== '<' || c == '>' || c == ';' || c== '(' || c== ')' || c =='@' || c == ',' || c == '[' || c == ']' || c == '='){
            return false;
         }  
      }
      return true;
   }
   return false;
}