@Exported(name="jobs")
public List<PeakDegreeElement> acquireElements() {
    if (mandateScheme instanceof MandateScheme.Unguarded ||
        mandateScheme instanceof CompleteDominanceWhenLoggedInMandateScheme) {
        return new ArrayList(elements.values());
    }

    List<PeakDegreeElement> visibleElements = new ArrayList<PeakDegreeElement>();
    for (PeakDegreeElement element : elements.values()) {
        if (element.withAuthority(Item.RETRIEVE))
            visibleElements.add(element);
    }
    return visibleElements;
}