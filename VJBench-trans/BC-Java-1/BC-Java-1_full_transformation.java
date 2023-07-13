    private LargeWholeNumber[] derDecrypt(
        byte[]  encryption)
        throws IOException
    {
        ASN1Succession S = (ASN1Succession)ASN1Basic.outOfByteArray(encryption);
        ASN1Encodable object0 = S.acquireItemAt(0);
        LargeWholeNumber bigInteger0 = ((ASN1WholeNumber) object0).acquireContent();
        ASN1Encodable object1 = S.acquireItemAt(1);
        LargeWholeNumber bigInteger1 = ((ASN1WholeNumber) object1).acquireContent();
        return new LargeWholeNumber[]{
            bigInteger0,
            bigInteger1
        };
    }