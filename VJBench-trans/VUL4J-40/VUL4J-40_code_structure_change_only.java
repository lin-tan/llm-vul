    public boolean isAllowed(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        ScimUser scimUserFromRequest = JsonUtils.readValue(IOUtils.toString(reader), ScimUser.class);

        String pathInfo = UaaUrlUtils.getRequestPath(request);
        IdentityZone zone = IdentityZoneHolder.get();
        ScimUser scimUserFromDb;

        try {
            scimUserFromDb = scimUserProvisioning.retrieve(
                UaaUrlUtils.extractPathVariableFromUrl(USER_ID_PATH_PARAMETER_INDEX, pathInfo), zone.getId());
        } catch (ScimResourceNotFoundException e) {
            return true;
        }

        String dbEmail = scimUserFromDb.getPrimaryEmail();
        String requestEmail = scimUserFromRequest.getPrimaryEmail();
        if (dbEmail.equals(requestEmail)) {

            String dbUserName = scimUserFromDb.getUserName();
            String requestUserName = scimUserFromRequest.getUserName();
            if (dbUserName.equals(requestUserName)) {

                if (scimUserFromDb.isVerified() == scimUserFromRequest.isVerified()) {

                    if (scimUserFromDb.isActive() == (scimUserFromRequest.isActive())) {

                        String dbOrigin = scimUserFromDb.getOrigin();
                        String requestOrigin = scimUserFromRequest.getOrigin();
                        if (dbOrigin.equals(requestOrigin)) {

                            return true;
                            
                        }else{

                            return false;

                        }
                        
                    }else{
                        return false;
                    }
                                        
                }else{
                    return false;
                }

            }else{
                return false;
            }    
            
        }else{

            return false;
        }
       
    }
