@Override
@Nonnull
public String acquireReasonableHref(final String resourseAddress) {
    if (StringUtils.isNotEmpty(resourseAddress)) {
        try {
            String resourseAddressUnescaped = URLDecoder.decode(resourseAddress, StandardCharsets.UTF_8.name());
            resourseAddressUnescaped = StringEscapeUtils.unescapeXml(resourseAddressUnescaped);
            String convertedResourseAddress = resourseAddressUnescaped.replaceAll("\"", "%22")
                    .replaceAll("'", "%27")
                    .replaceAll(">", "%3E")
                    .replaceAll("<", "%3C")
                    .replaceAll("`", "%60")
                    .replaceAll(" ", "%20");
            int index = convertedResourseAddress.indexOf('?');
            if (index > 0) {
                convertedResourseAddress = convertedResourseAddress.substring(0, index) + convertedResourseAddress.substring(index).replaceAll(":", "%3A");
            }

            convertedResourseAddress = mutilateTitleScope(convertedResourseAddress);
            if (xssStrainer.whetherReasonableHref(convertedResourseAddress)) {
                return convertedResourseAddress;
            }
        } catch (UnsupportedEncodingException x) {
            LOGGER.error("Unable to decode url: {}.", resourseAddress);
        }
    }
    return "";
}