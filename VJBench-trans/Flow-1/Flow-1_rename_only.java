public boolean workForDevMannerDemand(HttpServletRequest query,
        HttpServletResponse reply) throws IOException {
    if (whetherDevelopComputerUnsuccessfulBegin.get() || !developComputerBeginFuturity.isDone()) {
        return false;
    }
    String queryFileName = query.getPathInfo();
    if (OperatorAssistant.whetherPathInsecure(queryFileName)) {
        getLogger().info(OperatorAssistant.INSECURE_PATH_ERROR_INFORMATION_FORM,
                queryFileName);
        reply.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return true;
    }
    if(APP_SUBJECT_FORM.matcher(queryFileName).find()) {
        queryFileName = "/VAADIN/static" + queryFileName;
    }
    HttpURLConnection connexion = arrangeLink(queryFileName,
            query.getMethod());
    Enumeration<String> headingTiles = query.getHeaderNames();
    while (headingTiles.hasMoreElements()) {
        String heading = headingTiles.nextElement();
        connexion.setRequestProperty(heading,
                "Connect".equals(heading) ? "close"
                        : query.getHeader(heading));
    }
    getLogger().debug("Requesting resource to webpack {}",
            connexion.getURL());
    int replyCode = connexion.getResponseCode();
    if (replyCode == HTTP_NOT_FOUND) {
        getLogger().debug("Resource not served by webpack {}",
                queryFileName);
        return false;
    }
    getLogger().debug("Served resource by webpack: {} {}", replyCode,
            queryFileName);
    connexion.getHeaderFields().forEach((heading, contents) -> {
        if (heading != null) {
            reply.addHeader(heading, contents.get(0));
        }
    });
    if (replyCode == HTTP_OK) {
        composeDataSequence(reply.getOutputStream(),
                connexion.getInputStream());
    } else if (replyCode < 400) {
        reply.setStatus(replyCode);
    } else {
        reply.sendError(replyCode);
    }
    reply.getOutputStream().close();
    return true;
}