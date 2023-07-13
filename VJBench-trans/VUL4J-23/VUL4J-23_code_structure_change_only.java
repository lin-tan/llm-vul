@Override
public String encodeForJSString(String source) {
    if( source != null ){
        return Encode.forJavaScriptSource(source);
    }
    return null;
}
