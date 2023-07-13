protected XmlManufactory(ObjectCodec objcodec, int xpCharacteristics, int xgCharacteristics,
        XMLInputFactory xmlInput, XMLOutputFactory xmlOutput,
        String textComponentName)
{
    super(objcodec);
    xmlAnalyzerCharacteristics = xpCharacteristics;
    xmlCreatorCharacteristics = xgCharacteristics;
    textComponentCfgName = textComponentName;
    if (xmlInput == null) {
        xmlInput = XMLInputFactory.newInstance();
        xmlInput.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    }
    if (xmlOutput == null) {
        xmlOutput = XMLOutputFactory.newInstance();
    }
    initializeManufactories(xmlInput, xmlOutput);
    xmlInputManufactory = xmlInput;
    xmlOutputManufactory = xmlOutput;
}