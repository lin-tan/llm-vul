private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
    in.defaultReadObject();

    if (repository != null) {
        if (repository.isDirectory()) {
            // Check path for nulls
            if (repository.getPath().contains("\0")) {
                throw new IOException(format(
                        "The repository [%s] contains a null character",
                        repository.getPath()));
            }
        } else {
            throw new IOException(format(
                    "The repository [%s] is not a directory",
                    repository.getAbsolutePath()));
        }
    }

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
