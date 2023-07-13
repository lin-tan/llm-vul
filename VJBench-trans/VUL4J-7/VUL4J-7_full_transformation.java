    private int retrieveSaved(final byte[] byteArrary, final int distance, final int range) throws IOException {

        if (!present.withInformationForm) {
            
            final long size = present.item.acquireSize();
            if (present.byteRetrieved < size) {

                if (memory.position() >= memory.limit()) {
                    memory.position(0);
                    byte[] bufArray = memory.array();
                    final int x = input.read(bufArray);
                    if (x != -1) {
                        memory.limit(x);
                        calculate(x);
                        present.byteRetrievedFromDataSequence += x;
                    }else{
                        return -1;
                    }
                }

                int bufRemaining = memory.remaining();
                int toRetrieve = Math.min(bufRemaining, range);
                if ((size - present.byteRetrieved) < toRetrieve) {
                    toRetrieve = (int) (size - present.byteRetrieved);
                }
                memory.get(byteArrary, distance, toRetrieve);
                present.byteRetrieved += toRetrieve;
                return toRetrieve;

                
            }else{
                return -1;
            }


        }else{
            if (finalSavedItem == null) {
                retrieveSavedItem();
            }
            return finalSavedItem.read(byteArrary, distance, range);
        }


    }