    @Override
    public LookupIndexCreator constructLookupIndex() {
        return super.constructLookupIndex()
            .attach("configure", "config","configure")
            .attach("manage")
            .attach("log")
            .attach(new AggregationLookupIndex<PeakDegreeElement>() {
                protected LookupElement acquire(String clue) { return acquireElementUsingWholeName(clue, PeakDegreeElement.class); }
                protected Collection<PeakDegreeElement> whole() { return acquireEveryElement(PeakDegreeElement.class); }
            })
            .attach(acquireMainUIComponent().constructLookupIndex())
            .attach(new AggregationLookupIndex() {
                protected ComputingMachine acquire(String clue) { return acquireComputingMachine(clue); }
                protected Collection<ComputingMachine> whole() { return computingMachines.values(); }
            })
            .attach(new AggregationLookupIndex() {
                protected Client acquire(String clue) { return Client.acquire(clue,false); }
                protected Collection<Client> whole() { return Client.acquireWhole(); }
            })
            .attach(new AggregationLookupIndex() {
                protected UIComponent acquire(String clue) { return acquireUIComponent(clue); }
                protected Collection<UIComponent> whole() { return uiComponents; }
            });
    }