public static void checkDirectoryTraversal(@NonNull Path parentPath, @NonNull Path pathToCheck) {
    Assert.notNull(pathToCheck, "Path to check must not be null");
    Assert.notNull(parentPath, "Parent path must not be null");
    
    Path normalizedParentPath = parentPath.normalize();
    if (!pathToCheck.normalize().startsWith(normalizedParentPath)) {
        ForbiddenException e =  new ForbiddenException("You do not have permission to access " + pathToCheck);
        e.setErrorData(pathToCheck);
        throw e;
    }

}