
// import TypeConverter
protected Object doGetDocument(Exchange exchange, Object body) throws Exception {
    if (body != null) {

        Class<?> type = getDocumentType();
        Exception cause = null;
        Object answer = null;

        if (type != null) {
            try {
                CamelContext context = exchange.getContext();
                TypeConverter typeConverter = context.getTypeConverter();
                answer = typeConverter.convertTo(type, exchange, body);
            } catch (Exception e) {
                cause = e;
            }
        }

        if (type != null || answer != null) {
            if (answer == null) {
                if (cause == null) {
                    throw new NoTypeConversionAvailableException(body, type);
                } else {
                    throw cause;
                }
            } 

        }else{
            answer = body;
        }
    
        return answer;
    }

    return null;
}