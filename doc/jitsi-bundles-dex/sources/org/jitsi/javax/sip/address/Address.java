package org.jitsi.javax.sip.address;

import java.io.Serializable;
import java.text.ParseException;

public interface Address extends Cloneable, Serializable {
    Object clone();

    boolean equals(Object obj);

    String getDisplayName();

    URI getURI();

    int hashCode();

    boolean isWildcard();

    void setDisplayName(String str) throws ParseException;

    void setURI(URI uri);

    String toString();
}
