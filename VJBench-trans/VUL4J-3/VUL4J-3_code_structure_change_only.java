@Converter
public SAXSource toSAXSourceFromStream(StreamSource source, Exchange exchange) throws SAXException {
    XMLReader xmlReader = null;
    SAXParserFactory sfactory = null;
    
    try {
        if (!(exchange == null)) {
            sfactory = exchange.getProperty(Exchange.SAXPARSER_FACTORY, SAXParserFactory.class);
        }

        if (sfactory != null) {
            sfactory.setNamespaceAware(true);
            xmlReader = sfactory.newSAXParser().getXMLReader();
        }else{
            sfactory = SAXParserFactory.newInstance();
            try {
                sfactory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (Exception e) {
                LOG.warn("SAXParser doesn't support the feature {} with value {}, due to {}.", new Object[]{javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, "true", e});
            }
            sfactory.setNamespaceAware(true);
            xmlReader = sfactory.newSAXParser().getXMLReader();
        }
    
    } catch (Exception ex) {
        LOG.warn("Cannot create the SAXParser XMLReader, due to {}", ex);
    }
    Reader reader = source.getReader();
    InputSource inputSource = reader == null? new InputSource(source.getInputStream()):new InputSource(reader);
    String systemId = source.getSystemId();
    inputSource.setSystemId(systemId);
    String publicId = source.getPublicId();
    inputSource.setPublicId(publicId);

    return new SAXSource(xmlReader, inputSource);
}