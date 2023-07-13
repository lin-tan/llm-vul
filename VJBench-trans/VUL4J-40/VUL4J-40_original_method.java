public boolean isAllowed(HttpServletRequest request) throws IOException {
    String requestBody = IOUtils.toString(request.getReader());
    ScimUser scimUserFromRequest = JsonUtils.readValue(requestBody, ScimUser.class);
    String id = UaaUrlUtils.extractPathVariableFromUrl(USER_ID_PATH_PARAMETER_INDEX, UaaUrlUtils.getRequestPath(request));
    String zoneId = IdentityZoneHolder.get().getId();
    ScimUser scimUserFromDb;
    try {
        scimUserFromDb = scimUserProvisioning.retrieve(id, zoneId);
    } catch (ScimResourceNotFoundException e) {
        return true;
    }
    if (!scimUserFromDb.getPrimaryEmail().equals(scimUserFromRequest.getPrimaryEmail())) {
        return false;
    }
    if (!scimUserFromDb.getUserName().equals(scimUserFromRequest.getUserName())) {
        return false;
    }
    if (scimUserFromDb.isVerified() != scimUserFromRequest.isVerified()) {
        return false;
    }
    if (scimUserFromDb.isActive() != (scimUserFromRequest.isActive())) {
        return false;
    }
    if (!scimUserFromDb.getOrigin().equals(scimUserFromRequest.getOrigin())) {
        return false;
    }
    return true;
}
