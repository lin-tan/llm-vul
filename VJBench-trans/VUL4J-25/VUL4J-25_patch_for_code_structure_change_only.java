 @Override
    @Nonnull
    public String getValidHref(final String url) {
        if (!StringUtils.isNotEmpty(url)) {
            return "";
        }

        String encodedUrl = url.replaceAll("\"", "%22")
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
    
        return "";
    }