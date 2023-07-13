private LargeWholeNumber[] derDecrypt(
    byte[]  encryption)
    throws IOException
{
    ASN1Succession S = (ASN1Succession)ASN1Basic.outOfByteArray(encryption);
    return new LargeWholeNumber[]{
        ((ASN1WholeNumber)S.acquireItemAt(0)).acquireContent(),
        ((ASN1WholeNumber)S.acquireItemAt(1)).acquireContent()
    };
}