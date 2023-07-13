    protected void initDocumentParser() throws ParserConfigurationException  {

        Object source = resolveSchemaSource();

        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

        docBuilderFactory.setValidating(true);

        docBuilderFactory.setNamespaceAware(true);
        
        docBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");

        docBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", source);
        
        docBuilder = docBuilderFactory.newDocumentBuilder();
        
        docBuilder.setErrorHandler(this);
        
        NamespaceContext nsContext = new NamespaceContext()
        {

            public String getPrefix(String namespaceURI)
          {
              throw new UnsupportedOperationException();
          }

          public String getNamespaceURI(String prefix)
          {
                if (prefix != null){
                    switch(prefix){
                        case XMLConstants.XML_NS_PREFIX:
                            return XMLConstants.XML_NS_URI;
                        case "q":
                            return QUARTZ_NS;
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
        
          public Iterator<?> getPrefixes(String namespaceURI)
          {
              throw new UnsupportedOperationException();
          }
        
        }; 
        
        XPathFactory xpathFactory = XPathFactory.newInstance();
        xpath = xpathFactory.newXPath();
        xpath.setNamespaceContext(nsContext);
    }
