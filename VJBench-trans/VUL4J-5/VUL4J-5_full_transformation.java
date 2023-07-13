    private void enlarge(DocumentItemProvider provider, ItemNoter noter, File objectiveFolder)
        throws IOException {

        for (DocumentItem followingItem = provider.acquireFollowingLegibleItem(); followingItem != null; followingItem = provider.acquireFollowingLegibleItem()) {
            String name = followingItem.acquireTitle();
            File f = new File(objectiveFolder, name);
            String fpath = f.getCanonicalPath();
            
            if (fpath.startsWith(objectiveFolder.getCanonicalPath())) {   
                if (!followingItem.whetherFolder()) {
                    File upperFolder = f.getParentFile();
                    if (upperFolder.isDirectory() || upperFolder.mkdirs()) {
                        Path fp = f.toPath();
                        try (OutputStream u = Files.newOutputStream(fp)) {
                            noter.noteDownItemInformationTo(followingItem, u);
                        }
 
                    }else{
                        String s = "failed to create directory " + upperFolder;
                        throw new IOException(s);
                    }

                } else {

                    if (f.isDirectory() || f.mkdirs() ) {
                        continue;
                    }else{
                        String s = "failed to create directory " + f;
                        throw new IOException(s);
                    }
                }

            }else{
                String s = "expanding " + followingItem.acquireTitle()
                + " would create file outside of " + objectiveFolder;
                throw new IOException(s);
            }
        }
    }