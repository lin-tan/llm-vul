protected XmlManufactory(ObjectCodec objcodec, int xpCharacteristics, int xgCharacteristics,
        XMLInputFactory xmlInput, XMLOutputFactory xmlOutput,
        String textComponentName)
{
    super(objcodec);
    if (xmlOutput == null) {
        xmlOutput = XMLOutputFactory.newInstance();
    }
    
    xmlCreatorCharacteristics = xgCharacteristics;
    
    if (xmlInput == null) {
        xmlInput = XMLInputFactory.newInstance();
        xmlInput.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
    }

    textComponentCfgName = textComponentName;
    xmlAnalyzerCharacteristics = xpCharacteristics;
    initializeManufactories(xmlInput, xmlOutput);
    xmlOutputManufactory = xmlOutput;
    xmlInputManufactory = xmlInput;
   
}