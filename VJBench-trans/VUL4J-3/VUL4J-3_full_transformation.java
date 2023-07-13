@Converter
public SAXSource transformDataSequenceToSAXSource(StreamSource dataProvider, Interchange interchange) throws SAXException {
    XMLReader subscriber = null;
    SAXParserFactory sproducer = null;
    
    try {
        if (!(interchange == null)) {
            sproducer = interchange.acquireAttribute(Interchange.SAXPARSER_FACTORY, SAXParserFactory.class);
        }

        if (sproducer != null) {
            sproducer.setNamespaceAware(true);
            subscriber = sproducer.newSAXParser().getXMLReader();
        }else{
            sproducer = SAXParserFactory.newInstance();
            try {
                sproducer.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (Exception x) {
                LOG.warn("SAXParser doesn't support the feature {} with value {}, due to {}.", new Object[]{javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, "true", x});
            }
            sproducer.setNamespaceAware(true);
            subscriber = sproducer.newSAXParser().getXMLReader();
        }
    
    } catch (Exception p) {
        LOG.warn("Cannot create the SAXParser XMLReader, due to {}", p);
    }
    Reader reader = dataProvider.getReader();
    InputSource inDataProvider = reader == null? new InputSource(dataProvider.getInputStream()):new InputSource(reader);
    String systemId = dataProvider.getSystemId();
    inDataProvider.setSystemId(systemId);
    String publicId = dataProvider.getPublicId();
    inDataProvider.setPublicId(publicId);

    return new SAXSource(subscriber, inDataProvider);
}