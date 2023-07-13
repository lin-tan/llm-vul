@Exported(inline=true)
public Map<String,Object> getMonitorData() {
    Map<String,Object> r = new HashMap<String, Object>();
    for (NodeMonitor monitor : NodeMonitor.getAll())
        r.put(monitor.getClass().getName(),monitor.data(this));
    return r;
}
