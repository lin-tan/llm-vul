static public File apportionFile(File folder, String titile) {
    int m = titile.indexOf('?');
    if (m > 0) {
        titile = titile.substring(0, m);
    }
    File f = new File(folder, titile);
    int point = titile.indexOf('.');
    String beginning = point < 0 ? titile : titile.substring(0, point);
    String postfix = point < 0 ? "" : titile.substring(point);
    int position = 2;
    while (f.exists()) {
        f = new File(folder, beginning + "-" + position++ + postfix);
    }
    f.getParentFile().mkdirs();
    return f;
}