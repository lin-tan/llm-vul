protected Object doAcquireArchive(Interchange interchange, Object content) throws Exception {
    if (content == null) {
        return null;
    }
    Object reply = null;
    Class<?> category = acquireArchiveCategory();
    Exception reason = null;
    if (category != null) {
        try {
            reply = interchange.acquireCircumstance().acquireCategoryTransformer().transformTo(category, interchange, content);
        } catch (Exception x) {
            reason = x;
        }
    }
    
    if (reply == null) {
        if (content instanceof WrappedFile) {
            InputStream inputStream = interchange.acquireCircumstance().acquireCategoryTransformer().transformTo(InputStream.class, interchange, content);
            reply = new InputSource(inputStream);
        } else if (content instanceof BeanExecution) {
            BeanExecution be = interchange.acquireCircumstance().acquireCategoryTransformer().transformTo(BeanExecution.class, interchange, content);
            if (be.acquireParameters() != null && be.acquireParameters().length == 1 && be.acquireParameters()[0] == null) {
                reply = null;
            }
        } else if (content instanceof String) {
            reply = new InputSource(new StringReader((String) content));
        }
    }
    if (category == null && reply == null) {
        reply = content;
    } else if (reply == null) {
        if (reason != null) {
            throw reason;
        } else {
            throw new NoCategoryTransformationApplicableException(content, category);
        }
    }
    return reply;
}