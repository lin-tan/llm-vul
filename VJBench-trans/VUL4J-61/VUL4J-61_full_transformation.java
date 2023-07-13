    private Stream<MapOutcome> xmlXpathToMapOutcome(@Name("url") String URL, boolean easyManner, String way, Map<String, Object> setting) throws Exception {
        setting =  setting == null? Collections.emptyMap():setting;
        boolean breakDownAlongMistake = (boolean) setting.getOrDefault("failOnError", true);
        List<MapOutcome> outcome = new ArrayList<>();
        try {

            FileUtils.examineReadPermitted(URL);
            DocumentBuilderFactory archiveCreatorFactory = DocumentBuilderFactory.newInstance();

            XPathFactory xPathFactory = XPathFactory.newInstance();
            archiveCreatorFactory.setIgnoringElementContentWhitespace(true);
            archiveCreatorFactory.setNamespaceAware(true);

            if(StringUtils.isEmpty(way) ){
                way =  "/" ;
            }
           
            XPathExpression xPathExpression = xPathFactory.newXPath().compile(way);
            InputStream inputStream = Util.unlockInDataSequence(URL, (Map) setting.getOrDefault( "headers", Collections.emptyMap() ) , null);
            Document doc = archiveCreatorFactory.newDocumentBuilder().parse(inputStream);
            NodeList vertexListing = (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);

            int m = 0;
            while ( m < vertexListing.getLength()) {
                final Deque<Map<String, Object>> stack = new LinkedList<>();
                Node node_m = vertexListing.item(m);
                manageNode(stack, node_m, easyManner);
                int position = 0;
                while ( position < stack.size()) {
                    Map<String, Object> stack_first = stack.pollFirst();
                    outcome.add(new MapOutcome(stack_first));
                    position++;
                }

                m++;

            }
        }
        catch (FileNotFoundException x){
            if(breakDownAlongMistake){
                String message = x.getMessage();
                throw new FileNotFoundException(message);
            }else{
                Map<String, Object> emptyMap =Collections.emptyMap();
                MapOutcome mapResult = new MapOutcome(emptyMap);
                return Stream.of(mapResult);
            }
        }
        catch (Exception x){
            if(breakDownAlongMistake){
                throw new Exception(x);
            }else{
                Map<String, Object> emptyMap =Collections.emptyMap();
                MapOutcome mapResult = new MapOutcome(emptyMap);
                return Stream.of(mapResult);
            }
                
        }
        return outcome.stream();
    }