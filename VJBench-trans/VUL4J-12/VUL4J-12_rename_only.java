private int enlarge(int s, final int z) {
    int h = (1 << (z - 1));
    while (s < h) {
        h = (-1 << z) + 1;
        s += h;
    }
    return s;
}