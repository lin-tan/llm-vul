void setTo(Calendar c, int i) {
    int n = Math.min(i-offset, c.getActualMaximum(field));
    c.set(field, n);
}
