	public static void writeEntry(ZipFile zipFile, ZipEntry entry, File destDir)
		throws IOException
	{
		String entryName = entry.getName();
		File outFile = new File(destDir,entryName);

		if (!entry.isDirectory()) {

			File parentFile = outFile.getParentFile();
			parentFile.mkdirs();
			try (InputStream in = zipFile.getInputStream(entry)) {
				IOUtil.writeStream(in, outFile);
			}
		}
		else {
			outFile.mkdirs();
		}
	}
