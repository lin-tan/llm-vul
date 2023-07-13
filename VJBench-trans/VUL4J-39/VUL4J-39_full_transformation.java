    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
       
        s= source != null? s.append("remoteAddress=").append(source):s;
      
        if (customerIdentification != null) {
            
            s =s.length() > 0?s.append(", "):s;
            s.append("clientId=");
            s.append(customerIdentification);
        }
        if (sessionIdentification != null) {
            s =s.length() > 0?s.append(", "):s;
            s.append("sessionId=");
            s.append(sessionIdentification);
        }
        return s.toString();
    }