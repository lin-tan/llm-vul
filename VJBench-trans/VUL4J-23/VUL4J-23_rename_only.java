@Override
public String JavaScriptStringEncoding(String dataProvider) {
    return dataProvider == null ? null : Encode.forJavaScriptSource(dataProvider);
}
