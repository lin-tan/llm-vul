    @Override
    public SearchIndexBuilder makeSearchIndex() {
        return super.makeSearchIndex()
            .add("configure", "config","configure")
            .add("manage")
            .add("log")
            .add(new CollectionSearchIndex<TopLevelItem>() {
                protected SearchItem get(String key) { return getItemByFullName(key, TopLevelItem.class); }
                protected Collection<TopLevelItem> all() { return getAllItems(TopLevelItem.class); }
            })
            .add(getPrimaryView().makeSearchIndex())
            .add(new CollectionSearchIndex() {
                protected Computer get(String key) { return getComputer(key); }
                protected Collection<Computer> all() { return computers.values(); }
            })
            .add(new CollectionSearchIndex() {
                protected User get(String key) { return User.get(key,false); }
                protected Collection<User> all() { return User.getAll(); }
            })
            .add(new CollectionSearchIndex() {
                protected View get(String key) { return getView(key); }
                protected Collection<View> all() { return views; }
            });
    }
