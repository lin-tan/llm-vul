private void enlarge(DocumentItemProvider provider, ItemNoter noter, File objectiveFolder)
    throws IOException {
    String objectiveDirectoryPath = objectiveFolder.getCanonicalPath();
    DocumentItem followingItem = provider.acquireFollowingLegibleItem();
    while (followingItem != null) {
        File f = new File(objectiveFolder, followingItem.acquireTitle());
        if (!f.getCanonicalPath().startsWith(objectiveDirectoryPath)) {
            throw new IOException("expanding " + followingItem.acquireTitle()
                + " would create file outside of " + objectiveFolder);
        }
        if (followingItem.whetherFolder()) {
            if (!f.isDirectory() && !f.mkdirs()) {
                throw new IOException("failed to create directory " + f);
            }
        } else {
            File upperFolder = f.getParentFile();
            if (!upperFolder.isDirectory() && !upperFolder.mkdirs()) {
                throw new IOException("failed to create directory " + upperFolder);
            }
            try (OutputStream u = Files.newOutputStream(f.toPath())) {
                noter.noteDownItemInformationTo(followingItem, u);
            }
        }
        followingItem = provider.acquireFollowingLegibleItem();
    }
}