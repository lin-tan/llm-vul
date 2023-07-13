	public SecureRandom getObject() throws Exception {
		SecureRandom rnd = SecureRandom.getInstance(algorithm);
		rnd.nextBytes(new byte[1]);
		if (seed !=null) {
			InputStream inputStream = seed.getInputStream();
			rnd.setSeed(FileCopyUtils.copyToByteArray(inputStream));
		}
		return rnd;
	}
