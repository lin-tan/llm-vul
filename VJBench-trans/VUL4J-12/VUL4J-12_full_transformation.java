private int enlarge(int s, final int z) {
    for (int h = (1 << (z - 1)); s < h; s += h) {
        h = (-1 << z) + 1;
    }
    return s;
}