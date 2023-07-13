public static void checkDirectoryTraversal(@NonNull Path parentPath, @NonNull Path pathToCheck) {
    Assert.notNull(parentPath, "Parent path must not be null");
    Assert.notNull(pathToCheck, "Path to check must not be null");
    if (pathToCheck.startsWith(parentPath.normalize())) {
        return;
    }
    throw new ForbiddenException("You do not have permission to access " + pathToCheck).setErrorData(pathToCheck);
}
