@Exported(name="jobs")
public List<TopLevelItem> getItems() {
    Collection<TopLevelItem> itemValues = items.values();
    if (!(authorizationStrategy instanceof AuthorizationStrategy.Unsecured) &&
        !(authorizationStrategy instanceof FullControlOnceLoggedInAuthorizationStrategy)) {
        List<TopLevelItem> viewableItems = new ArrayList<TopLevelItem>();
        Iterator<TopLevelItem> iterator = itemValues.iterator();
        while( iterator.hasNext()){
            TopLevelItem item = iterator.next();
            if (!item.hasPermission(Item.READ))
                continue;
            viewableItems.add(item);
        }
        return viewableItems;
    }
    return new ArrayList(itemValues);
}