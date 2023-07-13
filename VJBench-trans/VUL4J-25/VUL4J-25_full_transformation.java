@Override
@Nonnull
public String acquireReasonableHref(final String resourseAddress) {
    if (!StringUtils.isNotEmpty(resourseAddress)) {
        return "";
    }

    try {
        String name = StandardCharsets.UTF_8.name();
        String convertedResourseAddress = StringEscapeUtils.unescapeXml(URLDecoder.decode(resourseAddress,name));
        convertedResourseAddress = convertedResourseAddress.replaceAll("\"", "%22");
        convertedResourseAddress = convertedResourseAddress.replaceAll("'", "%27");
        convertedResourseAddress = convertedResourseAddress.replaceAll(">", "%3E");
        convertedResourseAddress = convertedResourseAddress.replaceAll("<", "%3C");
        convertedResourseAddress = convertedResourseAddress.replaceAll("`", "%60");
        convertedResourseAddress = convertedResourseAddress.replaceAll(" ", "%20");
        int index = convertedResourseAddress.indexOf('?');
        convertedResourseAddress = index > 0? convertedResourseAddress.substring(0, index) + convertedResourseAddress.substring(index).replaceAll(":", "%3A"):convertedResourseAddress;
        convertedResourseAddress = mutilateTitleScope(convertedResourseAddress);
        if (xssStrainer.whetherReasonableHref(convertedResourseAddress)) {
            return convertedResourseAddress;
        }
    } catch (UnsupportedEncodingException x) {
        LOGGER.error("Unable to decode url: {}.", resourseAddress);
    }

    return "";
}