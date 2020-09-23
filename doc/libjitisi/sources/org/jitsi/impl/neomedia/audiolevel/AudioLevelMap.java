package org.jitsi.impl.neomedia.audiolevel;

public class AudioLevelMap {
    private long[][] levels = ((long[][]) null);

    public void putLevel(long csrc, int level) {
        long[][] levelsRef = this.levels;
        int csrcIndex = findCSRC(levelsRef, csrc);
        if (csrcIndex == -1) {
            this.levels = appendCSRCToMatrix(levelsRef, csrc, level);
        } else {
            levelsRef[csrcIndex][1] = (long) level;
        }
    }

    public boolean removeLevel(long csrc) {
        long[][] levelsRef = this.levels;
        int index = findCSRC(levelsRef, csrc);
        if (index == -1) {
            return false;
        }
        if (levelsRef.length == 1) {
            this.levels = (long[][]) null;
            return true;
        }
        long[][] newLevelsRef = new long[(levelsRef.length - 1)][];
        System.arraycopy(levelsRef, 0, newLevelsRef, 0, index);
        System.arraycopy(levelsRef, index + 1, newLevelsRef, index, newLevelsRef.length - index);
        this.levels = newLevelsRef;
        return true;
    }

    public int getLevel(long csrc) {
        long[][] levelsRef = this.levels;
        int index = findCSRC(levelsRef, csrc);
        if (index == -1) {
            return -1;
        }
        return (int) levelsRef[index][1];
    }

    private int findCSRC(long[][] levels, long csrc) {
        if (levels != null) {
            for (int i = 0; i < levels.length; i++) {
                if (levels[i][0] == csrc) {
                    return i;
                }
            }
        }
        return -1;
    }

    private long[][] appendCSRCToMatrix(long[][] levels, long csrc, int level) {
        int newLength = (levels == null ? 0 : levels.length) + 1;
        long[][] newLevels = new long[newLength][];
        newLevels[0] = new long[]{csrc, (long) level};
        if (newLength != 1) {
            System.arraycopy(levels, 0, newLevels, 1, levels.length);
        }
        return newLevels;
    }
}
