public static void examinePathManipulation(@NonNull Path basePath, @NonNull Path examinePath) {
    Assert.notNull(basePath, "Parent path must not be null");
    Assert.notNull(examinePath, "Path to check must not be null");
    if (examinePath.startsWith(basePath.normalize())) {
        return;
    }
    throw new ProhibitedException("You do not have permission to access " + examinePath).specifyErrorInformation(examinePath);
}