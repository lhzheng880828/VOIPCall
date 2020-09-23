package net.java.sip.communicator.impl.dns;

public class UnboundResult {
    byte[] answerPacket;
    boolean bogus;
    String canonname;
    byte[][] data;
    boolean haveData;
    boolean nxDomain;
    int qclass;
    String qname;
    int qtype;
    int rcode;
    boolean secure;
    String whyBogus;
}
