final static private XMLStreamReader createXMLStreamReader(InputStream inDataSequence) throws XMLStreamException, IOException {
    XMLInputFactory producer = XMLInputFactory.newInstance();
    producer.setProperty(XMLInputFactory.IS_COALESCING, true);
    producer.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, true);
    return producer.createXMLStreamReader(swatheBeginingOfStringDetachingInDataSequence(inDataSequence));
}