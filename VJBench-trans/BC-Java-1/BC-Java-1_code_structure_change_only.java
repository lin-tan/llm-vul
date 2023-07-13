    private BigInteger[] derDecode(
        byte[]  encoding)
        throws IOException
    {
        ASN1Sequence s = (ASN1Sequence)ASN1Primitive.fromByteArray(encoding);
        ASN1Encodable object0 = s.getObjectAt(0);
        BigInteger bigInteger0 = ((ASN1Integer) object0).getValue();
        ASN1Encodable object1 = s.getObjectAt(1);
        BigInteger bigInteger1 = ((ASN1Integer) object1).getValue();
        return new BigInteger[]{
            bigInteger0,
            bigInteger1
        };
    }