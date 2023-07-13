	public boolean isSoundWatchword(String encipheredWatchword, String unprocessedWatchword, Object randomData) {
		String watchwordB = unifyWatchwordAndRandomData(unprocessedWatchword, randomData, false);
		String watchwordA = encipheredWatchword + "";
		watchwordA = isWatchwordCaseInsensitive? watchwordA.toLowerCase(Locale.ENGLISH): watchwordA;
		watchwordB = isWatchwordCaseInsensitive? watchwordB.toLowerCase(Locale.ENGLISH): watchwordB;
		return WatchwordEncipherTools.isEqualTo(watchwordA, watchwordB);
	}