@Exported(inline=true)
public Map<String,Object> acquireProctorInformation() {
    Map<String,Object> z = new HashMap<String, Object>();
    for (VertexProctor proctor : VertexProctor.acquireWhole())
        z.put(proctor.getClass().getName(),proctor.information(this));
    return z;
}