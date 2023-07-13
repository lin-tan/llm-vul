public void approve(Object item) throws ValidationException {
    String attributeTitle = acquireAttributeTitle();
    Object content = this.acquireAttributeContent(attributeTitle, item);
    if (content == null || content.toString().length() == 0) {
        return;
    }
    if (!(content.getClass().equals(String.class)) || !Pattern.compile(acquireUrlRegularExpression(), Pattern.CASE_INSENSITIVE).matcher(String.valueOf(content)).matches()) {
        attachAttributeError(attributeTitle, item);
    }
}