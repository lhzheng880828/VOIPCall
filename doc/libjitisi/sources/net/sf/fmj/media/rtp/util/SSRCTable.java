package net.sf.fmj.media.rtp.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public class SSRCTable {
    static final int INCR = 16;
    Object[] objList = new Object[16];
    int[] ssrcList = new int[16];
    int total = 0;

    public synchronized Enumeration<Object> elements() {
        return new Enumeration<Object>() {
            int next = 0;

            public boolean hasMoreElements() {
                return this.next < SSRCTable.this.total;
            }

            public Object nextElement() {
                synchronized (SSRCTable.this) {
                    if (this.next < SSRCTable.this.total) {
                        Object[] objArr = SSRCTable.this.objList;
                        int i = this.next;
                        this.next = i + 1;
                        Object obj = objArr[i];
                        return obj;
                    }
                    throw new NoSuchElementException("SSRCTable Enumeration");
                }
            }
        };
    }

    public synchronized Object get(int ssrc) {
        Object obj;
        int i = indexOf(ssrc);
        if (i < 0) {
            obj = null;
        } else {
            obj = this.objList[i];
        }
        return obj;
    }

    public synchronized int getSSRC(Object obj) {
        int i;
        for (int i2 = 0; i2 < this.total; i2++) {
            if (this.objList[i2] == obj) {
                i = this.ssrcList[i2];
                break;
            }
        }
        i = 0;
        return i;
    }

    private int indexOf(int ssrc) {
        if (this.total <= 3) {
            if (this.total > 0 && this.ssrcList[0] == ssrc) {
                return 0;
            }
            if (this.total > 1 && this.ssrcList[1] == ssrc) {
                return 1;
            }
            if (this.total <= 2 || this.ssrcList[2] != ssrc) {
                return -1;
            }
            return 2;
        } else if (this.ssrcList[0] == ssrc) {
            return 0;
        } else {
            if (this.ssrcList[this.total - 1] == ssrc) {
                return this.total - 1;
            }
            int i = 0;
            int j = this.total - 1;
            do {
                int x = ((j - i) / 2) + i;
                if (this.ssrcList[x] == ssrc) {
                    return x;
                }
                if (ssrc > this.ssrcList[x]) {
                    i = x + 1;
                    continue;
                } else if (ssrc < this.ssrcList[x]) {
                    j = x;
                    continue;
                } else {
                    continue;
                }
            } while (i < j);
            return -1;
        }
    }

    public boolean isEmpty() {
        return this.total == 0;
    }

    public synchronized void put(int ssrc, Object obj) {
        if (this.total == 0) {
            this.ssrcList[0] = ssrc;
            this.objList[0] = obj;
            this.total = 1;
        } else {
            int i = 0;
            while (i < this.total) {
                if (this.ssrcList[i] < ssrc) {
                    i++;
                } else if (this.ssrcList[i] == ssrc) {
                    this.objList[i] = obj;
                }
            }
            if (this.total == this.ssrcList.length) {
                int[] sl = new int[(this.ssrcList.length + 16)];
                Object[] ol = new Object[(this.objList.length + 16)];
                if (i > 0) {
                    System.arraycopy(this.ssrcList, 0, sl, 0, this.total);
                    System.arraycopy(this.objList, 0, ol, 0, this.total);
                }
                this.ssrcList = sl;
                this.objList = ol;
            }
            for (int x = this.total - 1; x >= i; x--) {
                this.ssrcList[x + 1] = this.ssrcList[x];
                this.objList[x + 1] = this.objList[x];
            }
            this.ssrcList[i] = ssrc;
            this.objList[i] = obj;
            this.total++;
        }
    }

    public synchronized Object remove(int ssrc) {
        Object res = null;
        synchronized (this) {
            int i = indexOf(ssrc);
            if (i >= 0) {
                res = this.objList[i];
                while (i < this.total - 1) {
                    this.ssrcList[i] = this.ssrcList[i + 1];
                    this.objList[i] = this.objList[i + 1];
                    i++;
                }
                this.ssrcList[this.total - 1] = 0;
                this.objList[this.total - 1] = null;
                this.total--;
            }
        }
        return res;
    }

    public synchronized void removeAll() {
        for (int i = 0; i < this.total; i++) {
            this.ssrcList[i] = 0;
            this.objList[i] = null;
        }
        this.total = 0;
    }

    public synchronized void removeObj(Object obj) {
        if (obj != null) {
            int i = 0;
            while (i < this.total && this.objList[i] != obj) {
                i++;
            }
            if (i < this.total) {
                while (i < this.total - 1) {
                    this.ssrcList[i] = this.ssrcList[i + 1];
                    this.objList[i] = this.objList[i + 1];
                    i++;
                }
                this.ssrcList[this.total - 1] = 0;
                this.objList[this.total - 1] = null;
                this.total--;
            }
        }
    }

    public int size() {
        return this.total;
    }
}
