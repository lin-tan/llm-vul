    @Override
    public int putMistakeAttribute(PriorToComeInCase occurrence,
            ErrorAttribute<UndiscoveredException> attribute) {
        
        String extraInformation = attribute.withPersonalizedContent()? "Reason: " + attribute.acquirePersonalizedContent():"";
        Location location = occurrence.acquirePlacement();
        String way = location.acquirePath();
        way = Jsoup.clean(way, Whitelist.none());
        extraInformation = Jsoup.clean(extraInformation, Whitelist.none());

        UI ui = occurrence.acquireUI();
        VaadinSession session = ui.acquireSession();
        DeploymentConfiguration config = session.acquireArrangement();
        boolean productManner = config.beProductManner();

        String templet = acquireMistakeHypertextMarkupLanguage(productManner);
        templet = templet.replace("{{path}}", way);
        templet = templet.replace("{{additionalInfo}}", extraInformation);
        String routes = acquireWays(occurrence);
        templet = templet.contains("{{routes}}")? templet.replace("{{routes}}", routes): templet;
        
        com.vaadin.flow.dom.Component_ component = acquireComponent();
        HypertextMarkupLanguage html = new HypertextMarkupLanguage(templet);
        component.attachMember(html.acquireComponent());
        return HttpServletResponse.SC_NOT_FOUND;
    }