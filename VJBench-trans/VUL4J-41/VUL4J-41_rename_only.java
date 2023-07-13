protected void takeOutDataFile( final File originFile, final File folder, final InputStream compressedInDataSequence,
                            final String itemTitle, final Date itemTime, final boolean whetherFolder,
                            final Integer setting, String symbolicLinkTargetLocation )
    throws IOException, DocumentorException
{
    final File targetFile = FileUtils.resolveFile( folder, itemTitle );
    try
    {
        if ( !whetherOverwrite() && targetFile.exists() && ( targetFile.lastModified() >= itemTime.getTime() ) )
        {
            return;
        }
        final File parentFile = targetFile.getParentFile();
        if ( parentFile != null )
        {
            parentFile.mkdirs();
        }
        if ( !StringUtils.isEmpty( symbolicLinkTargetLocation ) )
        {
            SymlinkUtils.createSymbolicLink( targetFile, new File( symbolicLinkTargetLocation ) );
        }
        else if ( whetherFolder )
        {
            targetFile.mkdirs();
        }
        else
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
        targetFile.setLastModified( itemTime.getTime() );
        if ( !whetherDisregardConsent() && setting != null && !whetherFolder )
        {
            DocumentItemUtility.changeMode( targetFile, setting );
        }
    }
    catch ( final FileNotFoundException e )
    {
        acquireLogger().warn( "Unable to expand to file " + targetFile.getPath() );
    }
}