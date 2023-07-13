@Override
public String encodeForJSString(String dataProvider) {
    if( dataProvider != null ){
        return Encode.forJavaScript(source).replace("\\-", "\\u002D");
    }
    return null;
}
