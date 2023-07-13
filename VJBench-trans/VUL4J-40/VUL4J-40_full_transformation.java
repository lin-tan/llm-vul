    public boolean whetherPermitted(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        Customer CustomerOfRequest = JsonTools.retrieveContent(IOUtils.toString(reader), Customer.class);

        String pathInfo = URLTools.acquireRequestPath(request);
        IdentityZone zone = IndividualityAreaPossessor.acquire();
        Customer CustomerOfDatabase;

        try {
            CustomerOfDatabase = CustomerManagement.recover(
                URLTools.derivePathObjectFromURL(CUSTOMER_IDENTIFICATION_PATH_ARGUMENT_INDEX, pathInfo), zone.acquireIdentification());
        } catch (DataSourceUnfoundException x) {
            return true;
        }

        String dbEmail = CustomerOfDatabase.acquireMainEmail();
        String requestEmail = CustomerOfRequest.acquireMainEmail();
        if (dbEmail.equals(requestEmail)) {

            String dbUserName = CustomerOfDatabase.acquireCustomerTitle();
            String requestUserName = CustomerOfRequest.acquireCustomerTitle();
            if (dbUserName.equals(requestUserName)) {

                if (CustomerOfDatabase.whetherAuthenticated() == CustomerOfRequest.whetherAuthenticated()) {

                    if (CustomerOfDatabase.whetherInAction() == (CustomerOfRequest.whetherInAction())) {

                        String dbOrigin = CustomerOfDatabase.acquireSource();
                        String requestOrigin = CustomerOfRequest.acquireSource();
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