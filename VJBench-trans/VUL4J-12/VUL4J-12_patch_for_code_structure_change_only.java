private int extend(int v, final int t) {
  int vt = (1 << (t - 1));
    if (v < vt) {
        vt = (-1 << t) + 1;
        v += vt;
    }
    return v;
}