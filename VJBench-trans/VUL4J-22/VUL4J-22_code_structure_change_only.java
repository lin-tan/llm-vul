    public static String getContextPath(HttpServletRequest request) {
        String contextPath = (String) request.getAttribute(INCLUDE_CONTEXT_PATH_ATTRIBUTE);
        contextPath = contextPath == null? request.getContextPath(): contextPath;
    
        if (!"/".equals(contextPath)) 
            return decodeRequestString(request, contextPath);
        else
            return decodeRequestString(request, "");
    }