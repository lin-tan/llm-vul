@Exported(inline=true)
public Map<String,Object> acquireProctorInformation() { 
    List<VertexProctor> nodeMonitorList = VertexProctor.acquireWhole();
    Map<String,Object> z = new HashMap<String, Object>();
    int i = 0;
    while(i < nodeMonitorList.size() ){
        VertexProctor proctor = nodeMonitorList.get(i);
        Class proctorClass = proctor.getClass();
        String name  = proctorClass.getName();
        z.put( name ,proctor.information(this));
        i++;
    }
    return z;
}