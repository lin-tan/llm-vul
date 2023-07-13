@Converter
public SAXSource transformDataSequenceToSAXSource(StreamSource dataProvider, Interchange interchange) throws SAXException {
    InputSource inDataProvider;
    if (dataProvider.getReader() != null) {
        inDataProvider = new InputSource(dataProvider.getReader());
    } else {
        inDataProvider = new InputSource(dataProvider.getInputStream());
    }
    inDataProvider.setSystemId(dataProvider.getSystemId());
    inDataProvider.setPublicId(dataProvider.getPublicId());
    XMLReader subscriber = null;
    SAXParserFactory sproducer = null;
    try {
        if (interchange != null) {
            sproducer = interchange.acquireAttribute(Interchange.SAXPARSER_FACTORY, SAXParserFactory.class);
        }
        if (sproducer == null) {
            sproducer = SAXParserFactory.newInstance();
            try {
                sproducer.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (Exception x) {
                LOG.warn("SAXParser doesn't support the feature {} with value {}, due to {}.", new Object[]{javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, "true", x});
            }
        }
        sproducer.setNamespaceAware(true);
        SAXParser analyzer = sproducer.newSAXParser();
        subscriber = analyzer.getXMLReader();
    } catch (Exception p) {
        LOG.warn("Cannot create the SAXParser XMLReader, due to {}", p);
    }
    return new SAXSource(subscriber, inDataProvider);
}