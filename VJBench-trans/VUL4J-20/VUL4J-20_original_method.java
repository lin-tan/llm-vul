public float[] toFloatArray()
{
    float[] retval = new float[size()];
    for (int i = 0; i < size(); i++)
    {
        retval[i] = ((COSNumber)getObject( i )).floatValue();
    }
    return retval;
}
