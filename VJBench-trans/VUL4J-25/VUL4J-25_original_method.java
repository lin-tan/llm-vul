@Override
@Nonnull
public String getValidHref(final String url) {
    if (StringUtils.isNotEmpty(url)) {
        try {
            String unescapedURL = URLDecoder.decode(url, StandardCharsets.UTF_8.name());
            unescapedURL = StringEscapeUtils.unescapeXml(unescapedURL);
            String encodedUrl = unescapedURL.replaceAll("\"", "%22")
                    .replaceAll("'", "%27")
                    .replaceAll(">", "%3E")
                    .replaceAll("<", "%3C")
                    .replaceAll("`", "%60")
                    .replaceAll(" ", "%20");
            int qMarkIx = encodedUrl.indexOf('?');
            if (qMarkIx > 0) {
                encodedUrl = encodedUrl.substring(0, qMarkIx) + encodedUrl.substring(qMarkIx).replaceAll(":", "%3A");
            }

            encodedUrl = mangleNamespaces(encodedUrl);
            if (xssFilter.isValidHref(encodedUrl)) {
                return encodedUrl;
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Unable to decode url: {}.", url);
        }
    }
    return "";
}