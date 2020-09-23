package org.jitsi.impl.neomedia.conference;

import javax.media.protocol.DataSource;

public interface DataSourceFilter {
    boolean accept(DataSource dataSource);
}
