public boolean serveDevModeRequest(HttpServletRequest request,
        HttpServletResponse response) throws IOException {
    if (isDevServerFailedToStart.get() || !devServerStartFuture.isDone()) {
        return false;
    }
    String requestFilename = request.getPathInfo();
    if (HandlerHelper.isPathUnsafe(requestFilename)) {
        getLogger().info(HandlerHelper.UNSAFE_PATH_ERROR_MESSAGE_PATTERN,
                requestFilename);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return true;
    }
    if(APP_THEME_PATTERN.matcher(requestFilename).find()) {
        requestFilename = "/VAADIN/static" + requestFilename;
    }
    HttpURLConnection connection = prepareConnection(requestFilename,
            request.getMethod());
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
        String header = headerNames.nextElement();
        connection.setRequestProperty(header,
                "Connect".equals(header) ? "close"
                        : request.getHeader(header));
    }
    getLogger().debug("Requesting resource to webpack {}",
            connection.getURL());
    int responseCode = connection.getResponseCode();
    if (responseCode == HTTP_NOT_FOUND) {
        getLogger().debug("Resource not served by webpack {}",
                requestFilename);
        return false;
    }
    getLogger().debug("Served resource by webpack: {} {}", responseCode,
            requestFilename);
    connection.getHeaderFields().forEach((header, values) -> {
        if (header != null) {
            response.addHeader(header, values.get(0));
        }
    });
    if (responseCode == HTTP_OK) {
        writeStream(response.getOutputStream(),
                connection.getInputStream());
    } else if (responseCode < 400) {
        response.setStatus(responseCode);
    } else {
        response.sendError(responseCode);
    }
    response.getOutputStream().close();
    return true;
}
