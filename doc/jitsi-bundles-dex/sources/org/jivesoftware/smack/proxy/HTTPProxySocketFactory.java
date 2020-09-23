package org.jivesoftware.smack.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.SocketFactory;
import org.jitsi.gov.nist.core.Separators;
import org.jivesoftware.smack.proxy.ProxyInfo.ProxyType;
import org.jivesoftware.smack.util.Base64;

class HTTPProxySocketFactory extends SocketFactory {
    private static final Pattern RESPONSE_PATTERN = Pattern.compile("HTTP/\\S+\\s(\\d+)\\s(.*)\\s*");
    private ProxyInfo proxy;

    public HTTPProxySocketFactory(ProxyInfo proxy) {
        this.proxy = proxy;
    }

    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return httpProxifiedSocket(host, port);
    }

    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return httpProxifiedSocket(host, port);
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        return httpProxifiedSocket(host.getHostAddress(), port);
    }

    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return httpProxifiedSocket(address.getHostAddress(), port);
    }

    private Socket httpProxifiedSocket(String host, int port) throws IOException {
        String proxyLine;
        String proxyhost = this.proxy.getProxyAddress();
        Socket socket = new Socket(proxyhost, this.proxy.getProxyPort());
        String hostport = "CONNECT " + host + Separators.COLON + port;
        String username = this.proxy.getProxyUsername();
        if (username == null) {
            proxyLine = "";
        } else {
            proxyLine = "\r\nProxy-Authorization: Basic " + new String(Base64.encodeBytes((username + Separators.COLON + this.proxy.getProxyPassword()).getBytes("UTF-8")));
        }
        socket.getOutputStream().write((hostport + " HTTP/1.1\r\nHost: " + hostport + proxyLine + "\r\n\r\n").getBytes("UTF-8"));
        InputStream in = socket.getInputStream();
        StringBuilder got = new StringBuilder(100);
        int nlchars = 0;
        do {
            char c = (char) in.read();
            got.append(c);
            if (got.length() > 1024) {
                throw new ProxyException(ProxyType.HTTP, "Recieved header of >1024 characters from " + proxyhost + ", cancelling connection");
            } else if (c == 65535) {
                throw new ProxyException(ProxyType.HTTP);
            } else if ((nlchars == 0 || nlchars == 2) && c == 13) {
                nlchars++;
            } else if ((nlchars == 1 || nlchars == 3) && c == 10) {
                nlchars++;
            } else {
                nlchars = 0;
            }
        } while (nlchars != 4);
        if (nlchars != 4) {
            throw new ProxyException(ProxyType.HTTP, "Never received blank line from " + proxyhost + ", cancelling connection");
        }
        String response = new BufferedReader(new StringReader(got.toString())).readLine();
        if (response == null) {
            throw new ProxyException(ProxyType.HTTP, "Empty proxy response from " + proxyhost + ", cancelling");
        }
        Matcher m = RESPONSE_PATTERN.matcher(response);
        if (!m.matches()) {
            throw new ProxyException(ProxyType.HTTP, "Unexpected proxy response from " + proxyhost + ": " + response);
        } else if (Integer.parseInt(m.group(1)) == 200) {
            return socket;
        } else {
            throw new ProxyException(ProxyType.HTTP);
        }
    }
}
