private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
    in.defaultReadObject();
    OutputStream output = getOutputStream();
    if (cachedContent == null) {
        IOUtils.copy(new FileInputStream(dfosFile), output);
        dfosFile.delete();
        dfosFile = null;
    } else {
        output.write(cachedContent);
    }
    output.close();

    cachedContent = null;
}
