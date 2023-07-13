    protected void extractFile( final File srcF, final File dir, final InputStream compressedInputStream,
                                final String entryName, final Date entryDate, final boolean isDirectory,
                                final Integer mode, String symlinkDestination )
        throws IOException, ArchiverException
    {
       
        final File f = FileUtils.resolveFile( dir, entryName );

        try{
            if ( isOverwrite() || !f.exists() ||  f.lastModified() < entryDate.getTime() ) 
            {

                final File dirF = f.getParentFile();
                if ( dirF != null )
                {
                    dirF.mkdirs();
                }

                if ( StringUtils.isEmpty( symlinkDestination ) )
                {
        
                    if (! isDirectory )
                    {
                        OutputStream out = null;
                        try
                        {
                            out = new FileOutputStream( f );

                            IOUtil.copy( compressedInputStream, out );
                            out.close();
                            out = null;
                        }
                        finally
                        {
                            IOUtil.close( out );
                        }
                    }
                    else
                    {
                        f.mkdirs();
                  
                    }
                }
                else{
                    File symlinkDestFile = new File( symlinkDestination );
                    SymlinkUtils.createSymbolicLink( f, symlinkDestFile );
                }
                
        
                long time = entryDate.getTime();
                f.setLastModified( time );

                if ( !isIgnorePermissions() && mode != null && !isDirectory )
                {
                    ArchiveEntryUtils.chmod( f, mode );
                }

               
            }else{

                 return;

            }

          
        }
        catch ( final FileNotFoundException ex )
        {
            String path = f.getPath();
            getLogger().warn( "Unable to expand to file " + path );
        }
    }
