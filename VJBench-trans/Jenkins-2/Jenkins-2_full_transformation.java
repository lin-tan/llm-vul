@Exported(name="jobs")
public List<PeakDegreeElement> acquireElements() {
    Collection<PeakDegreeElement> itemValues = elements.values();
    if (!(mandateScheme instanceof MandateScheme.Unguarded) &&
        !(mandateScheme instanceof CompleteDominanceWhenLoggedInMandateScheme)) {
        List<PeakDegreeElement> visibleElements = new ArrayList<PeakDegreeElement>();
        Iterator<PeakDegreeElement> iterator = itemValues.iterator();
        while( iterator.hasNext()){
            PeakDegreeElement element = iterator.next();
            if (!element.withAuthority(Item.RETRIEVE))
                continue;
            visibleElements.add(element);
        }
        return visibleElements;
    }
    return new ArrayList(itemValues);
}