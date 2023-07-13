private Stream<MapOutcome> xmlXpathToMapOutcome(@Name("url") String URL, boolean easyManner, String way, Map<String, Object> setting) throws Exception {
    if (setting == null) setting = Collections.emptyMap();
    boolean breakDownAlongMistake = (boolean) setting.getOrDefault("failOnError", true);
    List<MapOutcome> outcome = new ArrayList<>();
    try {
        DocumentBuilderFactory archiveCreatorFactory = DocumentBuilderFactory.newInstance();
        archiveCreatorFactory.setNamespaceAware(true);
        archiveCreatorFactory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder archiveCreator = archiveCreatorFactory.newDocumentBuilder();

        FileUtils.examineReadPermitted(URL);
        Map<String, Object> heading = (Map) setting.getOrDefault( "headers", Collections.emptyMap() );
        Document doc = archiveCreator.parse(Util.unlockInDataSequence(URL, heading, null));
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        way = StringUtils.isEmpty(way) ? "/" : way;
        XPathExpression xPathExpression = xPath.compile(way);
        NodeList vertexListing = (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);
        for (int m = 0; m < vertexListing.getLength(); m++) {
            final Deque<Map<String, Object>> stack = new LinkedList<>();
            manageNode(stack, vertexListing.item(m), easyManner);
            for (int position = 0; position < stack.size(); position++) {
                outcome.add(new MapOutcome(stack.pollFirst()));
            }
        }
    }
    catch (FileNotFoundException x){
        if(!breakDownAlongMistake)
            return Stream.of(new MapOutcome(Collections.emptyMap()));
        else
            throw new FileNotFoundException(x.getMessage());
    }
    catch (Exception x){
        if(!breakDownAlongMistake)
            return Stream.of(new MapOutcome(Collections.emptyMap()));
        else
            throw new Exception(x);
    }
    return outcome.stream();
}