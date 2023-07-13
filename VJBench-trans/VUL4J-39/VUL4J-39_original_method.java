@Override
public String toString() {
    StringBuilder sb = new StringBuilder();
    if (origin != null) {
        sb.append("remoteAddress=").append(origin);
    }
    if (clientId != null) {
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append("clientId=").append(clientId);
    }
    if (sessionId != null) {
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append("sessionId=").append(sessionId);
    }
    return sb.toString();
}
