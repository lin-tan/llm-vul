    @Override
    public int setErrorParameter(BeforeEnterEvent event,
            ErrorParameter<NotFoundException> parameter) {
        
        String additionalInfo = parameter.hasCustomMessage()? "Reason: " + parameter.getCustomMessage():"";
        Location location = event.getLocation();
        String path = location.getPath();
        path = Jsoup.clean(path, Whitelist.none());
        additionalInfo = Jsoup.clean(additionalInfo, Whitelist.none());

        UI ui = event.getUI();
        VaadinSession session = ui.getSession();
        DeploymentConfiguration config = session.getConfiguration();
        boolean productionMode = config.isProductionMode();

        String template = getErrorHtml(productionMode);
        template = template.replace("{{path}}", path);
        template = template.replace("{{additionalInfo}}", additionalInfo);
        String routes = getRoutes(event);
        template = template.contains("{{routes}}")? template.replace("{{routes}}", routes): template;
        
        com.vaadin.flow.dom.Element element = getElement();
        Html html = new Html(template);
        element.appendChild(html.getElement());
        return HttpServletResponse.SC_NOT_FOUND;
    }
