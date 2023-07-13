protected XmlManufactory(ObjectCodec objcodec, int xpCharacteristics, int xgCharacteristics,
        XMLInputFactory xmlInput, XMLOutputFactory xmlOutput,
        String textComponentName)
{
    super(objcodec);
    xmlParserCharacteristics = xpCharacteristics;
    xmlCreatorCharacteristics = xgCharacteristics;
    textComponentCfgName = textComponentName;
    if (xmlInput == null) {
        xmlInput = XMLInputFactory.newInstance();
    }
    if (xmlOutput == null) {
        xmlOutput = XMLOutputFactory.newInstance();
    }
    initializeManufactories(xmlInput, xmlOutput);
    xmlInputManufactory = xmlInput;
    xmlOutputManufactory = xmlOutput;
}