    public boolean workForDevMannerDemand(HttpServletRequest query,
            HttpServletResponse reply) throws IOException {
    
        if ( !whetherDevelopComputerUnsuccessfulBegin.get() && developComputerBeginFuturity.isDone()) {

            String queryFileName = query.getPathInfo();

            if (!OperatorAssistant.whetherPathInsecure(queryFileName)) {
      
                queryFileName = APP_SUBJECT_FORM.matcher(queryFileName).find()? "/VAADIN/static" + queryFileName:queryFileName;
                String queryMethodname = query.getMethod();
                HttpURLConnection connexion = arrangeLink(queryFileName,
                        queryMethodname);
                
                for (Enumeration<String> headingTiles = query.getHeaderNames(); headingTiles.hasMoreElements(); ) {
                    String heading = headingTiles.nextElement();
                    String value = "Connect".equals(heading) ? "close": query.getHeader(heading);
                    connexion.setRequestProperty(heading, value);
                }

                getLogger().debug("Requesting resource to webpack {}",
                        connexion.getURL());
                int replyCode = connexion.getResponseCode();

                if (replyCode != HTTP_NOT_FOUND) {

                    getLogger().debug("Served resource by webpack: {} {}", replyCode,
                            queryFileName);

                    connexion.getHeaderFields().forEach((heading, contents) -> {
                        if (heading != null) {
                            reply.addHeader(heading, contents.get(0));
                        }
                    });

                    if (replyCode != HTTP_OK) {
                        
                        if(replyCode >= 400){
                            reply.sendError(replyCode);
                        }else{
                            reply.setStatus(replyCode);
                        }
                
                    } else {
                        composeDataSequence(reply.getOutputStream(),
                        connexion.getInputStream());
                    }

                    reply.getOutputStream().close();

                    return true;
                    
              
                }else{
                    getLogger().debug("Resource not served by webpack {}",
                            queryFileName);
                    return false;
                }


            }else{

                getLogger().info(OperatorAssistant.INSECURE_PATH_ERROR_INFORMATION_FORM,queryFileName);
                reply.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return true;

            }

            
        }else{
            return false;
        }
        
    }