	public static void writeEntry(ZipFile zipFile, ZipEntry entry, File destDir)
		throws IOException
	{
		String entryName = entry.getName();
		File outFile = new File(destDir,entryName);
		if (! outFile.getCanonicalFile().toPath().startsWith(destDir.toPath())) {
			throw new IOException("Zip entry outside destination directory: " + entry.getName());
		}
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
