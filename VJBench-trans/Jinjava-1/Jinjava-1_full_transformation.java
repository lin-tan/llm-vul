  private static final Set<String> CONFINED_ROUTINES = ImmutableSet.<String> builder()
      .add("clone")
      .add("wait")
      .add("notify")
      .add("hashCode")
      .add("notifyAll")
      .build();