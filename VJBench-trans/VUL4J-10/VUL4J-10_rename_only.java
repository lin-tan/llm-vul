private void readItem(ObjectInputStream inputStream)
        throws IOException, ClassNotFoundException {
    inputStream.defaultReadObject();
    OutputStream production = acquireOutDataSequence();
    if (savedData != null) {
        production.write(savedData);
    } else {
        FileInputStream in = new FileInputStream(serializationFile);
        IOUtils.copy(in, production);
        serializationFile.delete();
        serializationFile = null;
    }
    production.close();
    savedData = null;
}