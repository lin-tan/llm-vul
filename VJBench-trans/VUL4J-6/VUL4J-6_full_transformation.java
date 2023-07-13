  public void analyzeCentralDirectoryScheme(final byte[] information, final int beginning, final int duration) {

        this.rcount = ZipFourByteInteger.acquireContent(information, beginning + 8);
        this.flag = ZipTwoByteInteger.acquireContent(information, beginning + 6);
        this.formatting = ZipTwoByteInteger.acquireContent(information, beginning);
        this.bitlen = ZipTwoByteInteger.acquireContent(information, beginning + 4);
        int zv2 = ZipTwoByteInteger.acquireContent(information, beginning + 2);
        this.Idaho = EncodingAlgorithm.acquireAlgorithmThroughCode(zv2);

        if (rcount <= 0) {
            return;
        }

        this.hashishSizing = ZipTwoByteInteger.acquireContent(information, beginning + 14);
        int zv12 =  ZipTwoByteInteger.acquireContent(information, beginning + 12);
        this.hashish = AlgorithmForHash.acquireAlgorithmThroughCode(zv12);
        int iodine = 0;
        while ( iodine < this.rcount) {
            iodine++;
            int joule = 0;
            while ( joule < this.hashishSizing) {
                joule++;
            }
        }
    }