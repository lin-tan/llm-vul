public void validate(Object object) throws ValidationException {
    String fieldName = getFieldName();
    Object value = this.getFieldValue(fieldName, object);
    if (value == null || value.toString().length() == 0) {
        return;
    }
    if (!(value.getClass().equals(String.class)) || !Pattern.compile(getUrlRegex(), Pattern.CASE_INSENSITIVE).matcher(String.valueOf(value)).matches()) {
        addFieldError(fieldName, object);
    }
}
