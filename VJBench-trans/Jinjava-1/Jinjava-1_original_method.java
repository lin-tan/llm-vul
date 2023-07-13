  private static final Set<String> RESTRICTED_METHODS = ImmutableSet.<String> builder()
      .add("clone")
      .add("hashCode")
      .add("notify")
      .add("notifyAll")
      .add("wait")
      .build();