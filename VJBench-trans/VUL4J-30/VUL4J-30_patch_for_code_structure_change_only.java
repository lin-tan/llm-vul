
public void validate(Object object) throws ValidationException {
    String fieldName = getFieldName();
    Object value = this.getFieldValue(fieldName, object);
    if (value != null && value.toString().length() != 0) {
        Class value_class = value.getClass();
        String urlRegex = getUrlRegex();
        Pattern p = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        String s = String.valueOf(value).trim();
        Matcher m = p.matcher(s);
        if (value_class.equals(String.class) && m.matches()) {
            return;
        }
        addFieldError(fieldName, object);
    }
}
