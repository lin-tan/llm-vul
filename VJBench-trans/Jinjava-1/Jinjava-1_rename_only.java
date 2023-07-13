  private static final Set<String> CONFINED_ROUTINES = ImmutableSet.<String> builder()
      .add("clone")
      .add("hashCode")
      .add("notify")
      .add("notifyAll")
      .add("wait")
      .build();