package net.java.sip.communicator.util;

/**
 * Author:cl
 * Email:lhzheng@grandstream.cn
 * Date:20-9-15
 */
public class NetworkUtils {

    /**
     * The maximum int value that could correspond to a port number.
     */
    public static final int    MAX_PORT_NUMBER = 65535;

    /**
     * The minimum int value that could correspond to a port number bindable
     * by the SIP Communicator.
     */
    public static final int    MIN_PORT_NUMBER = 1024;

    /**
     * Determines whether <tt>port</tt> is a valid port number bindable by an
     * application (i.e. an integer between 1024 and 65535).
     *
     * @param port the port number that we'd like verified.
     *
     * @return <tt>true</tt> if port is a valid and bindable port number and
     * <tt>alse</tt> otherwise.
     */
    public static boolean isValidPortNumber(int port)
    {
        return MIN_PORT_NUMBER < port && port < MAX_PORT_NUMBER;
    }
}
