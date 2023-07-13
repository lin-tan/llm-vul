	public static void saveItem(ZipFile compressedFile, ZipEntry item, File targetFolder)
		throws IOException
	{
		String entryName = item.getName();
		File outputFile = new File(targetFolder,entryName);

		if (!item.isDirectory()) {

			File parentFile = outputFile.getParentFile();
			parentFile.mkdirs();
			try (InputStream input = compressedFile.getInputStream(item)) {
				InputOutputUtility.saveDataSequence(input, outputFile);
			}
		}
		else {
			outputFile.mkdirs();
		}
	}