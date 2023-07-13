@Override
@Nonnull
public String getValidHref(final String url) {
    if (!StringUtils.isNotEmpty(url)) {
        return "";
    }

    try {
        String name = StandardCharsets.UTF_8.name();
        String encodedUrl = StringEscapeUtils.unescapeXml(URLDecoder.decode(url,name));
        encodedUrl = encodedUrl.replaceAll("\"", "%22");
        encodedUrl = encodedUrl.replaceAll("'", "%27");
        encodedUrl = encodedUrl.replaceAll(">", "%3E");
        encodedUrl = encodedUrl.replaceAll("<", "%3C");
        encodedUrl = encodedUrl.replaceAll("`", "%60");
        encodedUrl = encodedUrl.replaceAll(" ", "%20");
        int qMarkIx = encodedUrl.indexOf('?');
        encodedUrl = qMarkIx > 0? encodedUrl.substring(0, qMarkIx) + encodedUrl.substring(qMarkIx).replaceAll(":", "%3A"):encodedUrl;
        encodedUrl = mangleNamespaces(encodedUrl);
        if (xssFilter.isValidHref(encodedUrl)) {
            return encodedUrl;
        }
    } catch (UnsupportedEncodingException e) {
        LOGGER.error("Unable to decode url: {}.", url);
    }

    return "";
}