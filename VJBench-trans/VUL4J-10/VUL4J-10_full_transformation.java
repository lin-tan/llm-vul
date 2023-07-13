private void readItem(ObjectInputStream inputStream)
        throws IOException, ClassNotFoundException {
    inputStream.defaultReadObject();
    OutputStream production = acquireOutDataSequence();
    if (savedData == null) {
        IOUtils.copy(new FileInputStream(serializationFile), production);
        serializationFile.delete();
        serializationFile = null;
    } else {
        production.write(savedData);
    }
    production.close();

    savedData = null;
}