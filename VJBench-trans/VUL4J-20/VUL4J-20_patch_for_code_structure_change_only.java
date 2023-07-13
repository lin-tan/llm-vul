
    public float[] toFloatArray()
    {
        int size = size();
        float[] retval = new float[size];
        int i = 0;
        while( i < size )
        {
             COSBase base = getObject(i);
            retval[i] =
                base instanceof COSNumber ? ((COSNumber) base).floatValue() : 0;
            i++;
        }
        return retval;
    }