    @Override
    public LookupIndexCreator constructLookupIndex() {
        LookupIndexCreator lookupIndexCreator = super.constructLookupIndex();

        AggregationLookupIndex<PeakDegreeElement> aggregationLookupIndexElement=new AggregationLookupIndex<PeakDegreeElement>() {
                protected LookupElement acquire(String clue) { 
                    return acquireElementUsingWholeName(clue, PeakDegreeElement.class); 
                }
                protected Collection<PeakDegreeElement> whole() { 
                    return acquireEveryElement(PeakDegreeElement.class); 
                }
        };


        AggregationLookupIndex aggregationLookupIndexComputingMachine = new AggregationLookupIndex() {
                protected Collection<ComputingMachine> whole() { 
                    return computingMachines.values(); 
                }
                protected ComputingMachine acquire(String clue) { 
                    return acquireComputingMachine(clue); 
                }

        };


        lookupIndexCreator.attach(aggregationLookupIndexElement);
        lookupIndexCreator.attach("configure", "config","configure");
        UIComponent mainUIComponent_ = acquireMainUIComponent();
        LookupIndexCreator mainUIComponentLookupIndexCreator = mainUIComponent_.constructLookupIndex();
        lookupIndexCreator.attach(mainUIComponentLookupIndexCreator);
        lookupIndexCreator.attach(aggregationLookupIndexComputingMachine); 

        lookupIndexCreator.attach("manage");
        lookupIndexCreator.attach("log");


         AggregationLookupIndex aggregationLookupIndexUIComponent = new AggregationLookupIndex() {
                protected Collection<UIComponent> whole() { 
                    return uiComponents; 
                }
                protected UIComponent acquire(String clue) { 
                    return acquireUIComponent(clue); 
                }
            };


           AggregationLookupIndex aggregationLookupIndexClient = new AggregationLookupIndex() {
                protected Collection<Client> whole() { 
                    return Client.acquireWhole(); 
                }
                protected Client acquire(String clue) { 
                    return Client.acquire(clue,false); 
                }
                
            };

        lookupIndexCreator.attach(aggregationLookupIndexClient);
            
        lookupIndexCreator.attach(aggregationLookupIndexUIComponent);

        return lookupIndexCreator;
            
        
    }