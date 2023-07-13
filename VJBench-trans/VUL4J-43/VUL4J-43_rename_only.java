public static void saveItem(ZipFile compressedFile, ZipEntry item, File targetFolder)
	throws IOException
{
	File outputFile = new File(targetFolder, item.getName());
	if (item.isDirectory()) {
		outputFile.mkdirs();
	}
	else {
		outputFile.getParentFile().mkdirs();
		try (InputStream input = compressedFile.getInputStream(item)) {
			InputOutputUtility.saveDataSequence(input, outputFile);
		}
	}
}