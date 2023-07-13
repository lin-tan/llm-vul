
    public float[] transformToFloatArray()
    {
        int length = length();
        float[] result = new float[length];
        int j = 0;
        while( j < length )
        {
            COSNumeral cosNumber = (COSNumeral)acquireItem( j );
            result[j] = cosNumber.floatContent();
            j++;
        }
        return result;
    }