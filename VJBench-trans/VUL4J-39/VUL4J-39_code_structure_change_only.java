    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
       
        sb= origin != null? sb.append("remoteAddress=").append(origin):sb;
      
        if (clientId != null) {
            
            sb =sb.length() > 0?sb.append(", "):sb;
            sb.append("clientId=");
            sb.append(clientId);
        }
        if (sessionId != null) {
            sb =sb.length() > 0?sb.append(", "):sb;
            sb.append("sessionId=");
            sb.append(sessionId);
        }
        return sb.toString();
    }
