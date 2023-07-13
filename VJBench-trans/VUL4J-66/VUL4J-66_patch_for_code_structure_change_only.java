   protected static boolean isValid(String str)
   {
      if (str != null && str.length() != 0){
         int i =0;
         while(i < str.length()) {
            char c = str.charAt(i);
            i++;
            if (c == '\n' ||c == '/'|| c=='\\' || c == '?' || c == ':' || c== '<' || c == '>' || c == ';' || c== '(' || c== ')' || c =='@' || c == ',' || c == '[' || c == ']' || c == '='){
               return false;
            }
            
         }

         return true;

      }
      
      return false;

   }
