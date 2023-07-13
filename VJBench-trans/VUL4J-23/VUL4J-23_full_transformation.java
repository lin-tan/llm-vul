@Override
public String JavaScriptStringEncoding(String dataProvider) {
    if( dataProvider != null ){
        return Encode.forJavaScriptSource(dataProvider);
    }
    return null;
}
