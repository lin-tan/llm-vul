
    public float[] toFloatArray()
    {
        int size = size();
        float[] retval = new float[size];
        int i = 0;
        while( i < size )
        {
            COSNumber cosNumber = (COSNumber)getObject( i );
            retval[i] = cosNumber.floatValue();
            i++;
        }
        return retval;
    }