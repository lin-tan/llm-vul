public static void writeEntry(ZipFile zipFile, ZipEntry entry, File destDir)
	throws IOException
{
	File outFile = new File(destDir, entry.getName());
	if (entry.isDirectory()) {
		outFile.mkdirs();
	}
	else {
		outFile.getParentFile().mkdirs();
		try (InputStream in = zipFile.getInputStream(entry)) {
			IOUtil.writeStream(in, outFile);
		}
	}
}
