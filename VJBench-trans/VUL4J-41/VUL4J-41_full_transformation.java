    protected void takeOutDataFile( final File originFile, final File folder, final InputStream compressedInDataSequence,
                                final String itemTitle, final Date itemTime, final boolean whetherFolder,
                                final Integer setting, String symbolicLinkTargetLocation )
        throws IOException, DocumentorException
    {
       
        final File targetFile = FileUtils.resolveFile( folder, itemTitle );

        try{
            if ( whetherOverwrite() || !targetFile.exists() ||  targetFile.lastModified() < itemTime.getTime() ) 
            {

                final File parentFile = targetFile.getParentFile();
                if ( parentFile != null )
                {
                    parentFile.mkdirs();
                }

                if ( StringUtils.isEmpty( symbolicLinkTargetLocation ) )
                {
        
                    if (! whetherFolder )
                    {
                        OutputStream output = null;
                        try
                        {
                            output = new FileOutputStream( targetFile );

                            IOUtil.copy( compressedInDataSequence, output );
                            output.close();
                            output = null;
                        }
                        finally
                        {
                            IOUtil.close( output );
                        }
                    }
                    else
                    {
                        targetFile.mkdirs();
                  
                    }
                }
                else{
                    File symlinkDestFile = new File( symbolicLinkTargetLocation );
                    SymlinkUtils.createSymbolicLink( targetFile, symlinkDestFile);
                }
                
        
                long time = itemTime.getTime();
                targetFile.setLastModified( time );

                if ( !whetherDisregardConsent() && setting != null && !whetherFolder )
                {
                    DocumentItemUtility.changeMode( targetFile, setting );
                }

               
            }else{

                 return;

            }

          
        }
        catch ( final FileNotFoundException e )
        {
            String path = targetFile.getPath();
            acquireLogger().warn( "Unable to expand to file " + path );
        }
    }