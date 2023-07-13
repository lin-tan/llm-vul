private int extend(int v, final int t) {
    int vt = (1 << (t - 1));
    while (v < vt) {
        vt = (-1 << t) + 1;
        v += vt;
    }
    return v;
}
