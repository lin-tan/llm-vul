  JaxbResponseConverter(JAXBContext context, Class<T> type) {
    this.type = type;
    this.context = context;
  }