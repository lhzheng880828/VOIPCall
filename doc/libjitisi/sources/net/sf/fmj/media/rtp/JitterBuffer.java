package net.sf.fmj.media.rtp;

import javax.media.Buffer;

class JitterBuffer {
    private int capacity;
    private Buffer[] elements;
    private int length;
    private int locked;
    private int offset;

    public JitterBuffer(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity");
        }
        this.elements = new Buffer[capacity];
        for (int i = 0; i < this.elements.length; i++) {
            this.elements[i] = new Buffer();
        }
        this.capacity = capacity;
        this.length = 0;
        this.locked = -1;
        this.offset = 0;
    }

    public void addPkt(Buffer buffer) {
        assertLocked(buffer);
        if (noMoreFree()) {
            throw new IllegalStateException("noMoreFree");
        }
        long firstSN = getFirstSeq();
        long lastSN = getLastSeq();
        long bufferSN = buffer.getSequenceNumber();
        if (firstSN == Buffer.SEQUENCE_UNKNOWN && lastSN == Buffer.SEQUENCE_UNKNOWN) {
            append(buffer);
        } else if (bufferSN < firstSN) {
            prepend(buffer);
        } else if (firstSN < bufferSN && bufferSN < lastSN) {
            insert(buffer);
        } else if (bufferSN > lastSN) {
            append(buffer);
        } else {
            returnFree(buffer);
        }
        this.locked = -1;
    }

    private void append(Buffer buffer) {
        int index = (this.offset + this.length) % this.capacity;
        if (index != this.locked) {
            this.elements[this.locked] = this.elements[index];
            this.elements[index] = buffer;
        }
        this.length++;
    }

    private void assertLocked(Buffer buffer) throws IllegalStateException {
        if (this.locked == -1) {
            throw new IllegalStateException("No Buffer has been retrieved from this JitterBuffer and has not been returned yet.");
        } else if (buffer != this.elements[this.locked]) {
            throw new IllegalArgumentException("buffer");
        }
    }

    private void assertNotLocked() throws IllegalStateException {
        if (this.locked != -1) {
            throw new IllegalStateException("A Buffer has been retrieved from this JitterBuffer and has not been returned yet.");
        }
    }

    /* access modifiers changed from: 0000 */
    public void dropFill(int index) {
        assertNotLocked();
        if (index < 0 || index >= this.length) {
            throw new IndexOutOfBoundsException(Integer.toString(index));
        }
        index = (this.offset + index) % this.capacity;
        Buffer buffer = this.elements[index];
        if (index == this.offset) {
            this.offset = (this.offset + 1) % this.capacity;
        } else if (index != ((this.offset + this.length) - 1) % this.capacity) {
            while (index != this.offset) {
                int i = index - 1;
                if (i < 0) {
                    i = this.capacity - 1;
                }
                this.elements[index] = this.elements[i];
                index = i;
            }
            this.elements[index] = buffer;
            this.offset = (this.offset + 1) % this.capacity;
        }
        this.length--;
        this.locked = index;
        returnFree(buffer);
    }

    public void dropFirstFill() {
        returnFree(getFill());
    }

    /* access modifiers changed from: 0000 */
    public boolean fillNotEmpty() {
        return getFillCount() != 0;
    }

    /* access modifiers changed from: 0000 */
    public boolean freeNotEmpty() {
        return getFreeCount() != 0;
    }

    public synchronized int getCapacity() {
        return this.capacity;
    }

    public Buffer getFill() {
        assertNotLocked();
        if (noMoreFill()) {
            throw new IllegalStateException("noMoreFill");
        }
        int index = this.offset;
        Buffer buffer = this.elements[index];
        this.offset = (this.offset + 1) % this.capacity;
        this.length--;
        this.locked = index;
        return buffer;
    }

    public Buffer getFill(int index) {
        if (index >= 0 && index < this.length) {
            return this.elements[(this.offset + index) % this.capacity];
        }
        throw new IndexOutOfBoundsException(Integer.toString(index));
    }

    public synchronized int getFillCount() {
        return this.length;
    }

    public long getFirstSeq() {
        return this.length == 0 ? Buffer.SEQUENCE_UNKNOWN : this.elements[this.offset].getSequenceNumber();
    }

    public Buffer getFree() {
        assertNotLocked();
        if (noMoreFree()) {
            throw new IllegalStateException("noMoreFree");
        }
        int index = (this.offset + this.length) % this.capacity;
        Buffer buffer = this.elements[index];
        this.locked = index;
        return buffer;
    }

    public int getFreeCount() {
        return this.capacity - this.length;
    }

    public long getLastSeq() {
        return this.length == 0 ? Buffer.SEQUENCE_UNKNOWN : this.elements[((this.offset + this.length) - 1) % this.capacity].getSequenceNumber();
    }

    private void insert(Buffer buffer) {
        int i = this.offset;
        int end = (this.offset + this.length) % this.capacity;
        long bufferSN = buffer.getSequenceNumber();
        while (i != end && this.elements[i].getSequenceNumber() <= bufferSN) {
            i++;
            if (i >= this.capacity) {
                i = 0;
            }
        }
        if (i == this.offset) {
            prepend(buffer);
        } else if (i == end) {
            append(buffer);
        } else {
            this.elements[this.locked] = this.elements[end];
            int j = end;
            while (j != i) {
                int k = j - 1;
                if (k < 0) {
                    k = this.capacity - 1;
                }
                this.elements[j] = this.elements[k];
                j = k;
            }
            this.elements[i] = buffer;
            this.length++;
        }
    }

    /* access modifiers changed from: 0000 */
    public boolean noMoreFill() {
        return getFillCount() == 0;
    }

    /* access modifiers changed from: 0000 */
    public boolean noMoreFree() {
        return getFreeCount() == 0;
    }

    private void prepend(Buffer buffer) {
        int index = this.offset - 1;
        if (index < 0) {
            index = this.capacity - 1;
        }
        if (index != this.locked) {
            this.elements[this.locked] = this.elements[index];
            this.elements[index] = buffer;
        }
        this.offset = index;
        this.length++;
    }

    public void returnFree(Buffer buffer) {
        assertLocked(buffer);
        this.locked = -1;
    }

    public void setCapacity(int capacity) {
        assertNotLocked();
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity");
        } else if (this.capacity != capacity) {
            int i;
            Buffer[] elements = new Buffer[capacity];
            while (getFillCount() > capacity) {
                dropFirstFill();
            }
            int length = Math.min(getFillCount(), capacity);
            for (i = 0; i < length; i++) {
                elements[i] = getFill(i);
            }
            for (i = length; i < capacity; i++) {
                elements[i] = new Buffer();
            }
            this.capacity = capacity;
            this.elements = elements;
            this.length = length;
            this.offset = 0;
        }
    }
}
