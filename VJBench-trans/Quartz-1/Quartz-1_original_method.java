protected void initDocumentParser() throws ParserConfigurationException  {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setNamespaceAware(true);
    docBuilderFactory.setValidating(true);
    docBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
    docBuilderFactory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", resolveSchemaSource());
    docBuilder = docBuilderFactory.newDocumentBuilder();
    
    docBuilder.setErrorHandler(this);
    NamespaceContext nsContext = new NamespaceContext()
    {
      public String getNamespaceURI(String prefix)
      {
          if (prefix == null)
              throw new IllegalArgumentException("Null prefix");
          if (XMLConstants.XML_NS_PREFIX.equals(prefix))
              return XMLConstants.XML_NS_URI;
          if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix))
              return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
          if ("q".equals(prefix))
              return QUARTZ_NS;
          return XMLConstants.NULL_NS_URI;
      }
      public Iterator<?> getPrefixes(String namespaceURI)
      {
          throw new UnsupportedOperationException();
      }
      public String getPrefix(String namespaceURI)
      {
          throw new UnsupportedOperationException();
      }
    }; 
    xpath = XPathFactory.newInstance().newXPath();
    xpath.setNamespaceContext(nsContext);
}
