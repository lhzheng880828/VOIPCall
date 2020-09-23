package net.sf.fmj.media.util;

import javax.media.Format;

/* compiled from: Resource */
class FormatTable {
    public int[] hits;
    public Format[] keys;
    public int last = 0;
    public Format[][] table;

    public FormatTable(int size) {
        this.keys = new Format[size];
        this.table = new Format[size][];
        this.hits = new int[size];
    }

    public int findLeastHit() {
        int min = this.hits[0];
        int idx = 0;
        for (int i = 1; i < this.last; i++) {
            if (this.hits[i] < min) {
                min = this.hits[i];
                idx = i;
            }
        }
        return idx;
    }

    /* access modifiers changed from: 0000 */
    public Format[] get(Format input) {
        Format[] res = null;
        int i = 0;
        while (i < this.last) {
            if (res == null && this.keys[i].matches(input)) {
                res = this.table[i];
                this.hits[i] = this.keys.length;
            } else {
                this.hits[i] = this.hits[i] - 1;
            }
            i++;
        }
        return res;
    }

    public void save(Format input, Format[] supported) {
        int idx;
        if (this.last >= this.keys.length) {
            idx = findLeastHit();
        } else {
            idx = this.last;
            this.last++;
        }
        this.keys[idx] = input;
        this.table[idx] = supported;
        this.hits[idx] = this.keys.length;
    }
}
