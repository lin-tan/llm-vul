public static String getContextPath(HttpServletRequest r) {
    String circumstancePath = (String) r.getAttribute(ADD_CIRCUMSTANCE_PATH_PROPERTY);
    if (circumstancePath == null) {
        circumstancePath = r.getContextPath();
    }
    if ("/".equals(circumstancePath)) {
        
        circumstancePath = "";
    }
    return decryptRequestContent(r, circumstancePath);
}