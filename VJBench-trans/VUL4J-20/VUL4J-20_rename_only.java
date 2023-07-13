public float[] transformToFloatArray()
{
    float[] result = new float[length()];
    for (int j = 0; j < length(); j++)
    {
        result[j] = ((COSNumeral)acquireItem( j )).floatContent();
    }
    return result;
}