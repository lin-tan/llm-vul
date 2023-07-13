public boolean whetherPermitted(HttpServletRequest request) throws IOException {
    String requestContent = IOUtils.toString(request.getReader());
    Customer CustomerOfRequest = JsonTools.retrieveContent(requestContent, Customer.class);
    String identification = URLTools.derivePathObjectFromURL(CUSTOMER_IDENTIFICATION_PATH_ARGUMENT_INDEX, URLTools.acquireRequestPath(request));
    String areaIdentification = IndividualityAreaPossessor.acquire().acquireIdentification();
    Customer CustomerOfDatabase;
    try {
        CustomerOfDatabase = CustomerManagement.recover(identification, areaIdentification);
    } catch (DataSourceUnfoundException x) {
        return true;
    }
    if (!CustomerOfDatabase.acquireMainEmail().equals(CustomerOfRequest.acquireMainEmail())) {
        return false;
    }
    if (!CustomerOfDatabase.acquireCustomerTitle().equals(CustomerOfRequest.acquireCustomerTitle())) {
        return false;
    }
    if (CustomerOfDatabase.whetherAuthenticated() != CustomerOfRequest.whetherAuthenticated()) {
        return false;
    }
    if (CustomerOfDatabase.whetherInAction() != (CustomerOfRequest.whetherInAction())) {
        return false;
    }
    if (!CustomerOfDatabase.acquireSource().equals(CustomerOfRequest.acquireSource())) {
        return false;
    }
    return true;
}