    private BigInteger[] derDecode(
        byte[]  encoding)
        throws IOException
    {
        ASN1Sequence s = (ASN1Sequence)ASN1Primitive.fromByteArray(encoding);
        if (s.size() != 2)
        {
            throw new IOException("malformed signature");
        }
        ASN1Encodable object0 = s.getObjectAt(0);
        ASN1Encodable object1 = s.getObjectAt(1);
        BigInteger bigInteger0 = ((ASN1Integer) object0).getValue();
        BigInteger bigInteger1 = ((ASN1Integer) object1).getValue();
        return new BigInteger[]{
            bigInteger0,
            bigInteger1
        };
    }