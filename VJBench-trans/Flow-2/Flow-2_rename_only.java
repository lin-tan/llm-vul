@Override
public int putMistakeAttribute(PriorToComeInCase occurrence,
        ErrorAttribute<UndiscoveredException> attribute) {
    String way = occurrence.acquirePlacement().acquirePath();
    String extraInformation = "";
    if (attribute.withPersonalizedContent()) {
        extraInformation = "Reason: " + attribute.acquirePersonalizedContent();
    }
    way = Jsoup.clean(way, Whitelist.none());
    extraInformation = Jsoup.clean(extraInformation, Whitelist.none());
    boolean productManner = occurrence.acquireUI().acquireSession().acquireArrangement()
            .beProductManner();
    String templet = acquireMistakeHypertextMarkupLanguage(productManner);
    templet = templet.replace("{{path}}", way);
    templet = templet.replace("{{additionalInfo}}", extraInformation);
    if (templet.contains("{{routes}}")) {
        templet = templet.replace("{{routes}}", acquireWays(occurrence));
    }

    acquireComponent().attachMember(new HypertextMarkupLanguage(templet).acquireComponent());
    return HttpServletResponse.SC_NOT_FOUND;
}