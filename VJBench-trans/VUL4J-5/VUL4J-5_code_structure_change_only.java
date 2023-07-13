    private void expand(ArchiveEntrySupplier supplier, EntryWriter writer, File targetDirectory)
        throws IOException {

        for (ArchiveEntry nextEntry = supplier.getNextReadableEntry(); nextEntry != null; nextEntry = supplier.getNextReadableEntry()) {
            String name = nextEntry.getName();
            File f = new File(targetDirectory, name);
            String fpath = f.getCanonicalPath();
            
            if (fpath.startsWith(targetDirectory.getCanonicalPath())) {   
                if (!nextEntry.isDirectory()) {
                    File parent = f.getParentFile();
                    if (parent.isDirectory() || parent.mkdirs()) {
                        Path fp = f.toPath();
                        try (OutputStream o = Files.newOutputStream(fp)) {
                            writer.writeEntryDataTo(nextEntry, o);
                        }
 
                    }else{
                        String s = "failed to create directory " + parent;
                        throw new IOException(s);
                    }

                } else {

                    if (f.isDirectory() || f.mkdirs()) {
                        continue;
                    }else{
                        String s = "failed to create directory " + f;
                        throw new IOException(s);
                    }
                }

            }else{
                String s = "expanding " + nextEntry.getName()
                + " would create file outside of " + targetDirectory;
                throw new IOException(s);
            }
        }
    }

