@Exported(name="jobs")
public List<TopLevelItem> getItems() {
    if (authorizationStrategy instanceof AuthorizationStrategy.Unsecured ||
        authorizationStrategy instanceof FullControlOnceLoggedInAuthorizationStrategy) {
        return new ArrayList(items.values());
    }

    List<TopLevelItem> viewableItems = new ArrayList<TopLevelItem>();
    for (TopLevelItem item : items.values()) {
        if (item.hasPermission(Item.READ))
            viewableItems.add(item);
    }
    return viewableItems;
}
