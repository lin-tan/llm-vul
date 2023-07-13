    protected XmlManufactory(ObjectCodec objcodec, int xpCharacteristics, int xgCharacteristics,
            XMLInputFactory xmlInput, XMLOutputFactory xmlOutput,
            String textComponentName)
    {
        super(objcodec);
        if (xmlOutput == null) {
            xmlOutput =  XMLOutputFactory.newInstance();
        }

        xmlCreatorCharacteristics = xgCharacteristics;
       
        if (xmlInput == null) {
            xmlInput = XMLInputFactory.newInstance();
        }

        textComponentCfgName = textComponentName;
        xmlParserCharacteristics = xpCharacteristics;
        initializeManufactories(xmlInput, xmlOutput);
        xmlOutputManufactory = xmlOutput;
        xmlInputManufactory = xmlInput;

        
    }