@Override
public String encodeForJSString(String source) {
    return source == null ? null : Encode.forJavaScriptSource(source);
}
