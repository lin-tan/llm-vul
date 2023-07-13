    protected XmlFactory(ObjectCodec oc, int xpFeatures, int xgFeatures,
            XMLInputFactory xmlIn, XMLOutputFactory xmlOut,
            String nameForTextElem)
    {
        super(oc);
        if (xmlOut == null) {
            xmlOut =  XMLOutputFactory.newInstance();
        }

        _xmlGeneratorFeatures = xgFeatures;
       
        if (xmlIn == null) {
            xmlIn = XMLInputFactory.newInstance();
        }

        _cfgNameForTextElement = nameForTextElem;
        _xmlParserFeatures = xpFeatures;
        _initFactories(xmlIn, xmlOut);
        _xmlOutputFactory = xmlOut;
        _xmlInputFactory = xmlIn;

        
    }
