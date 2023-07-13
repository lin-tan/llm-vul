  JaxbResponseConverter(JAXBContext context, Class<T> type) {
    this.type = type;
    this.context = context;
     xmlInputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
  }