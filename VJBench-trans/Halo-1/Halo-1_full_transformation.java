public static void examinePathManipulation(@NonNull Path basePath, @NonNull Path examinePath) {
    Assert.notNull(examinePath, "Path to check must not be null");
    Assert.notNull(basePath, "Parent path must not be null");
    
    Path normalizedBasePath = basePath.normalize();
    if (!examinePath.startsWith(normalizedBasePath)) {
        ProhibitedException e =  new ProhibitedException("You do not have permission to access " + examinePath);
        e.specifyErrorInformation(examinePath);
        throw e;
    }

}