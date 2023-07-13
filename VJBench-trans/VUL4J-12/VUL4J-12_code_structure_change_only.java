private int extend(int v, final int t) {
    for (int vt = (1 << (t - 1)); v < vt; v += vt) {
        vt = (-1 << t) + 1;
    }
    return v;
}