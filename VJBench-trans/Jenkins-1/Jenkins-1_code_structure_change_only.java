@Exported(inline=true)
public Map<String,Object> getMonitorData() { 
    List<NodeMonitor> nodeMonitorList = NodeMonitor.getAll();
    Map<String,Object> r = new HashMap<String, Object>();
    int i = 0;
    while(i < nodeMonitorList.size() ){
        NodeMonitor monitor = nodeMonitorList.get(i);
        Class monitorClass = monitor.getClass();
        String name  = monitorClass.getName();
        r.put( name ,monitor.data(this));
        i++;
    }
    return r;
}
