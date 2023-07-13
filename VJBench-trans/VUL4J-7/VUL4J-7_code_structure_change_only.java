    private int readStored(final byte[] buffer, final int offset, final int length) throws IOException {

        if (!current.hasDataDescriptor) {
            
            final long csize = current.entry.getSize();
            if (current.bytesRead < csize) {

                if (buf.position() >= buf.limit()) {
                    buf.position(0);
                    byte[] bufArray = buf.array();
                    final int l = in.read(bufArray);
                    if (l != -1) {
                        buf.limit(l);
                        count(l);
                        current.bytesReadFromStream += l;
                    }else{
                        return -1;
                    }
                }

                int bufRemaining = buf.remaining();
                int toRead = Math.min(bufRemaining, length);
                if ((csize - current.bytesRead) < toRead) {
                    toRead = (int) (csize - current.bytesRead);
                }
                buf.get(buffer, offset, toRead);
                current.bytesRead += toRead;
                return toRead;

                
            }else{
                return -1;
            }


        }else{
            if (lastStoredEntry == null) {
                readStoredEntry();
            }
            return lastStoredEntry.read(buffer, offset, length);
        }


    }
