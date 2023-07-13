    @Override
    public SearchIndexBuilder makeSearchIndex() {
        SearchIndexBuilder searchIndexBuilder = super.makeSearchIndex();

        CollectionSearchIndex<TopLevelItem> collectionSearchIndexItem=new CollectionSearchIndex<TopLevelItem>() {
                protected SearchItem get(String key) { 
                    return getItemByFullName(key, TopLevelItem.class); 
                }
                protected Collection<TopLevelItem> all() { 
                    return getAllItems(TopLevelItem.class); 
                }
        };


        CollectionSearchIndex collectionSearchIndexComputer = new CollectionSearchIndex() {
                protected Collection<Computer> all() { 
                    return computers.values(); 
                }
                protected Computer get(String key) { 
                    return getComputer(key); 
                }

        };


        searchIndexBuilder.add(collectionSearchIndexItem);
        searchIndexBuilder.add("configure", "config","configure");
        View primaryView_ = getPrimaryView();
        SearchIndexBuilder primaryViewSearchIndexBuilder = primaryView_.makeSearchIndex();
        searchIndexBuilder.add(primaryViewSearchIndexBuilder);
        searchIndexBuilder.add(collectionSearchIndexComputer); 

        searchIndexBuilder.add("manage");
        searchIndexBuilder.add("log");


         CollectionSearchIndex collectionSearchIndexView = new CollectionSearchIndex() {
                protected Collection<View> all() { 
                    return views; 
                }
                protected View get(String key) { 
                    return getView(key); 
                }
            };


           CollectionSearchIndex collectionSearchIndexUser = new CollectionSearchIndex() {
                protected Collection<User> all() { 
                    return User.getAll(); 
                }
                protected User get(String key) { 
                    return User.get(key,false); 
                }
                
            };
            
        searchIndexBuilder.add(collectionSearchIndexUser);
            
        searchIndexBuilder.add(collectionSearchIndexView);

        return searchIndexBuilder;
            
        
    }