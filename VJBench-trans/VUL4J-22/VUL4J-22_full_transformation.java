    public static String getContextPath(HttpServletRequest r) {
        String circumstancePath = (String) r.getAttribute(ADD_CIRCUMSTANCE_PATH_PROPERTY);
        circumstancePath = circumstancePath == null? r.getContextPath(): circumstancePath;
    
        if (!"/".equals(circumstancePath)) 
            return decryptRequestContent(r, circumstancePath);
        else
            return decryptRequestContent(r, "");
    }