public SecureRandom getObject() throws Exception {
	SecureRandom rnd = SecureRandom.getInstance(algorithm);
	if (seed != null) {
		byte[] seedBytes = FileCopyUtils.copyToByteArray(seed.getInputStream());
		rnd.setSeed(seedBytes);
	}
	else {
		rnd.nextBytes(new byte[1]);
	}

	return rnd;
}
