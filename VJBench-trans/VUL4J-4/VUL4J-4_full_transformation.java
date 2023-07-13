protected Object doAcquireArchive(Interchange interchange, Object content) throws Exception {
    if (content != null) {

        Class<?> category = acquireArchiveCategory();
        Exception reason = null;
        Object reply = null;

        if (category != null) {
            try {
                CamelContext context = interchange.acquireCircumstance();
                TypeConverter typeConverter = context.acquireCategoryTransformer();
                reply = typeConverter.transformTo(category, interchange, content);
            } catch (Exception x) {
                reason = x;
            }
        }

        if (reply == null) {
            if (!(content instanceof WrappedFile)) {
                if (!(content instanceof BeanExecution)) {
                    if (content instanceof String) {
                        StringReader stringReader = new StringReader((String) content);
                        reply = new InputSource(stringReader);
                    }
                }else{
                    CamelContext context = interchange.acquireCircumstance();
                    TypeConverter typeConverter = context.acquireCategoryTransformer();
                    BeanExecution be = typeConverter.transformTo(BeanExecution.class, interchange, content);
                    if (be.acquireParameters() != null && be.acquireParameters().length == 1 && be.acquireParameters()[0] == null) {
                        reply = null;
                    }
                }
            }else{
                CamelContext context = interchange.acquireCircumstance();
                TypeConverter typeConverter = context.acquireCategoryTransformer();
                reply = new InputSource(typeConverter.transformTo(InputStream.class, interchange, content));
            }
        }

        if (category != null || reply != null) {
            if (reply == null) {
                if (reason == null) {
                    throw new NoCategoryTransformationApplicableException(content, category);
                } else {
                    throw reason;
                }
            } 
        }else{
            reply = content;
        }  
        return reply;
    }

    return null;
}