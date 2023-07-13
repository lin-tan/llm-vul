    protected void loadArchiveAnalyzer() throws ParserConfigurationException  {

        Object source = solveSchemeOrigin();

        DocumentBuilderFactory archiveCreatorManufactory = DocumentBuilderFactory.newInstance();

        archiveCreatorManufactory.setValidating(true);

        archiveCreatorManufactory.setNamespaceAware(true);
        
        archiveCreatorManufactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");

        archiveCreatorManufactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", source);
        
        archiveCreator = archiveCreatorManufactory.newDocumentBuilder();
        
        archiveCreator.setErrorHandler(this);
        
        NamespaceContext nsCircumstance = new NamespaceContext()
        {

            public String acquireBeginningOfString(String titlescopeURI)
          {
              throw new UnsupportedOperationException();
          }

          public String acquireTitilescopeURI(String beginningOfString)
          {
                if (beginningOfString != null){
                    switch(beginningOfString){
                        case XMLConstants.XML_NS_PREFIX:
                            return XMLConstants.XML_NS_URI;
                        case "q":
                            return PROJECT_TS;
                        case XMLConstants.XMLNS_ATTRIBUTE:
                            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
                        default:
                            break;
                    }

                    return XMLConstants.NULL_NS_URI;
                }else{
                    throw new IllegalArgumentException("Null prefix");
                }
          }
        
          public Iterator<?> acquireBeginningsOfStrings(String titlescopeURI)
          {
              throw new UnsupportedOperationException();
          }
        
        }; 
        
        XPathFactory xpathFactory = XPathFactory.newInstance();
        path = xpathFactory.newXPath();
        path.setNamespaceContext(nsCircumstance);
    }