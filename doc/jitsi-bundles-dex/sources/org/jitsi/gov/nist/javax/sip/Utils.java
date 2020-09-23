package org.jitsi.gov.nist.javax.sip;

import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.clientauthutils.DigestServerAuthenticationHelper;
import org.jitsi.gov.nist.javax.sip.message.SIPResponse;

public class Utils implements UtilsExt {
    private static int callIDCounter;
    private static long counter = 0;
    private static MessageDigest[] digesterPool = new MessageDigest[digesterPoolsSize];
    private static int digesterPoolsSize = 20;
    private static Utils instance = new Utils();
    private static Random rand = new Random(System.nanoTime());
    private static String signature = toHexString(Integer.toString(Math.abs(rand.nextInt() % 1000)).getBytes());
    private static final char[] toHex = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    static {
        int q = 0;
        while (q < digesterPoolsSize) {
            try {
                digesterPool[q] = MessageDigest.getInstance(DigestServerAuthenticationHelper.DEFAULT_ALGORITHM);
                q++;
            } catch (Exception ex) {
                throw new RuntimeException("Could not intialize Digester ", ex);
            }
        }
    }

    public static Utils getInstance() {
        return instance;
    }

    public static String toHexString(byte[] b) {
        int pos = 0;
        char[] c = new char[(b.length * 2)];
        for (int i = 0; i < b.length; i++) {
            int i2 = pos + 1;
            c[pos] = toHex[(b[i] >> 4) & 15];
            pos = i2 + 1;
            c[i2] = toHex[b[i] & 15];
        }
        return new String(c);
    }

    public static String getQuotedString(String str) {
        return '\"' + str.replace(Separators.DOUBLE_QUOTE, "\\\"") + '\"';
    }

    protected static String reduceString(String input) {
        String newString = input.toLowerCase();
        int len = newString.length();
        String retval = "";
        int i = 0;
        while (i < len) {
            if (!(newString.charAt(i) == ' ' || newString.charAt(i) == 9)) {
                retval = retval + newString.charAt(i);
            }
            i++;
        }
        return retval;
    }

    public static String toUpperCase(String str) {
        return str.toUpperCase(Locale.ENGLISH);
    }

    public String generateCallIdentifier(String address) {
        String str;
        long random = rand.nextLong();
        MessageDigest md = digesterPool[(int) Math.abs(random % ((long) digesterPoolsSize))];
        synchronized (md) {
            long nanoTime = System.nanoTime() + System.currentTimeMillis();
            int i = callIDCounter;
            callIDCounter = i + 1;
            str = toHexString(md.digest(Long.toString((nanoTime + ((long) i)) + random).getBytes())) + Separators.AT + address;
        }
        return str;
    }

    public synchronized String generateTag() {
        return Integer.toHexString(rand.nextInt());
    }

    public String generateBranchId() {
        String str;
        long nextLong = rand.nextLong();
        long j = counter;
        counter = 1 + j;
        long num = ((nextLong + j) + System.currentTimeMillis()) + System.nanoTime();
        MessageDigest digester = digesterPool[(int) Math.abs(num % ((long) digesterPoolsSize))];
        synchronized (digester) {
            str = "z9hG4bK-" + signature + "-" + toHexString(digester.digest(Long.toString(num).getBytes()));
        }
        return str;
    }

    public boolean responseBelongsToUs(SIPResponse response) {
        String branch = response.getTopmostVia().getBranch();
        return branch != null && branch.startsWith("z9hG4bK-" + signature);
    }

    public static String getSignature() {
        return signature;
    }

    public static void main(String[] args) {
        final HashSet branchIds = new HashSet();
        Executor e = Executors.newFixedThreadPool(100);
        for (int q = 0; q < 100; q++) {
            e.execute(new Runnable() {
                public void run() {
                    for (int b = 0; b < 1000000; b++) {
                        String bid = Utils.getInstance().generateBranchId();
                        if (branchIds.contains(bid)) {
                            throw new RuntimeException("Duplicate Branch ID");
                        }
                        branchIds.add(bid);
                    }
                }
            });
        }
        System.out.println("Done!!");
    }
}
