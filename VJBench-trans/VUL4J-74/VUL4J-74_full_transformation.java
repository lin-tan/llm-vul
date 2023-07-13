public SecureRandom acquireItem() throws Exception {
	SecureRandom secureRandomInstance = SecureRandom.getInstance(algorithm);

	if (source == null) {
		secureRandomInstance.nextBytes(new byte[1]);
	}
	else {
		InputStream inputStream = source.getInputStream();
		secureRandomInstance.setSeed(FileCopyUtils.copyToByteArray(inputStream));
	}
	return secureRandomInstance;
}