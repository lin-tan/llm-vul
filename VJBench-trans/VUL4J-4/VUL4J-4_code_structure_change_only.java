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

        if (answer == null) {
            if (!(body instanceof WrappedFile)) {
                if (!(body instanceof BeanInvocation)) {
                    if (body instanceof String) {
                        StringReader stringReader = new StringReader((String) body);
                        answer = new InputSource(stringReader);
                    }
                }else{
                    CamelContext context = exchange.getContext();
                    TypeConverter typeConverter = context.getTypeConverter();
                    BeanInvocation bi = typeConverter.convertTo(BeanInvocation.class, exchange, body);
                    if (bi.getArgs() != null && bi.getArgs().length == 1 && bi.getArgs()[0] == null) {
                        answer = null;
                    }
                }
            }else{
                CamelContext context = exchange.getContext();
                TypeConverter typeConverter = context.getTypeConverter();
                answer = new InputSource(typeConverter.convertTo(InputStream.class, exchange, body));
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