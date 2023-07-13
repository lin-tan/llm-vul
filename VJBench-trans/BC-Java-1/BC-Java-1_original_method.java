private BigInteger[] derDecode(
    byte[]  encoding)
    throws IOException
{
    ASN1Sequence s = (ASN1Sequence)ASN1Primitive.fromByteArray(encoding);
    return new BigInteger[]{
        ((ASN1Integer)s.getObjectAt(0)).getValue(),
        ((ASN1Integer)s.getObjectAt(1)).getValue()
    };
}
