private int retrieveSaved(final byte[] byteArrary, final int distance, final int range) throws IOException {
    if (present.withInformationForm) {
        if (finalSavedItem == null) {
            retrieveSavedItem();
        }
        return finalSavedItem.read(byteArrary, distance, range);
    }
    final long size = present.item.acquireSize();
    if (present.byteRetrieved >= size) {
        return -1;
    }
    if (memory.position() >= memory.limit()) {
        memory.position(0);
        final int x = input.read(memory.array());
        if (x == -1) {
            return -1;
        }
        memory.limit(x);
        calculate(x);
        present.byteRetrievedFromDataSequence += x;
    }
    int toRetrieve = Math.min(memory.remaining(), range);
    if ((size - present.byteRetrieved) < toRetrieve) {
        toRetrieve = (int) (size - present.byteRetrieved);
    }
    memory.get(byteArrary, distance, toRetrieve);
    present.byteRetrieved += toRetrieve;
    return toRetrieve;
}