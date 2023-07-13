@Converter
public SAXSource toSAXSourceFromStream(StreamSource source, Exchange exchange) throws SAXException {
    InputSource inputSource;
    if (source.getReader() != null) {
        inputSource = new InputSource(source.getReader());
    } else {
        inputSource = new InputSource(source.getInputStream());
    }
    inputSource.setSystemId(source.getSystemId());
    inputSource.setPublicId(source.getPublicId());
    XMLReader xmlReader = null;
    SAXParserFactory sfactory = null;
    try {
        if (exchange != null) {
            sfactory = exchange.getProperty(Exchange.SAXPARSER_FACTORY, SAXParserFactory.class);
        }
        if (sfactory == null) {
            sfactory = SAXParserFactory.newInstance();
            try {
                sfactory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (Exception e) {
                LOG.warn("SAXParser doesn't support the feature {} with value {}, due to {}.", new Object[]{javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, "true", e});
            }
        }
        sfactory.setNamespaceAware(true);
        SAXParser parser = sfactory.newSAXParser();
        xmlReader = parser.getXMLReader();
    } catch (Exception ex) {
        LOG.warn("Cannot create the SAXParser XMLReader, due to {}", ex);
    }
    return new SAXSource(xmlReader, inputSource);
}
