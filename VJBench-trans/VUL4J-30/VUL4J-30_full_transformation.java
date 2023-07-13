public void approve(Object item) throws ValidationException {
    String attributeTitle = acquireAttributeTitle();
    Object content = this.acquireAttributeContent(attributeTitle, item);
    if (content != null && content.toString().length() != 0) {
        Class content_class = content.getClass();
        String urlRegex = acquireUrlRegularExpression();
        Pattern p = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        String s = String.valueOf(content);
        Matcher m = p.matcher(s);
        if (content_class.equals(String.class) && m.matches()) {
            return;
        }
        attachAttributeError(attributeTitle, item);
    }
}