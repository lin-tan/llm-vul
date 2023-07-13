protected Object doGetDocument(Exchange exchange, Object body) throws Exception {
    if (body == null) {
        return null;
    }
    Object answer = null;
    Class<?> type = getDocumentType();
    Exception cause = null;
    if (type != null) {
        try {
            answer = exchange.getContext().getTypeConverter().convertTo(type, exchange, body);
        } catch (Exception e) {
            cause = e;
        }
    }
    
    if (answer == null) {
        if (body instanceof WrappedFile) {
            InputStream is = exchange.getContext().getTypeConverter().convertTo(InputStream.class, exchange, body);
            answer = new InputSource(is);
        } else if (body instanceof BeanInvocation) {
            BeanInvocation bi = exchange.getContext().getTypeConverter().convertTo(BeanInvocation.class, exchange, body);
            if (bi.getArgs() != null && bi.getArgs().length == 1 && bi.getArgs()[0] == null) {
                answer = null;
            }
        } else if (body instanceof String) {
            answer = new InputSource(new StringReader((String) body));
        }
    }
    if (type == null && answer == null) {
        answer = body;
    } else if (answer == null) {
        if (cause != null) {
            throw cause;
        } else {
            throw new NoTypeConversionAvailableException(body, type);
        }
    }
    return answer;
}
