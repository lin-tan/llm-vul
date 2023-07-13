    public boolean serveDevModeRequest(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
    
        if ( !isDevServerFailedToStart.get() && devServerStartFuture.isDone()) {

            String requestFilename = request.getPathInfo();

            if (!HandlerHelper.isPathUnsafe(requestFilename)) {
      
                requestFilename = APP_THEME_PATTERN.matcher(requestFilename).find()? "/VAADIN/static" + requestFilename:requestFilename;
                String requestMethodname = request.getMethod();
                HttpURLConnection connection = prepareConnection(requestFilename,
                        requestMethodname);
                
                for (Enumeration<String> headerNames = request.getHeaderNames(); headerNames.hasMoreElements(); ) {
                    String header = headerNames.nextElement();
                    String value = "Connect".equals(header) ? "close": request.getHeader(header);
                    connection.setRequestProperty(header, value);
                }

                getLogger().debug("Requesting resource to webpack {}",
                        connection.getURL());
                int responseCode = connection.getResponseCode();

                if (responseCode != HTTP_NOT_FOUND) {

                    getLogger().debug("Served resource by webpack: {} {}", responseCode,
                            requestFilename);

                    connection.getHeaderFields().forEach((header, values) -> {
                        if (header != null) {
                            response.addHeader(header, values.get(0));
                        }
                    });

                    if (responseCode != HTTP_OK) {
                        
                        if(responseCode >= 400){
                            response.sendError(responseCode);
                        }else{
                            response.setStatus(responseCode);
                        }
                
                    } else {
                        writeStream(response.getOutputStream(),
                        connection.getInputStream());
                    }

                    response.getOutputStream().close();

                    return true;
                    
              
                }else{
                    getLogger().debug("Resource not served by webpack {}",
                            requestFilename);
                    return false;
                }


            }else{

                getLogger().info(HandlerHelper.UNSAFE_PATH_ERROR_MESSAGE_PATTERN,requestFilename);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return true;

            }

            
        }else{
            return false;
        }
        
    }
