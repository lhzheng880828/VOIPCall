package net.sf.fmj.media.content.merge;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.IncompatibleSourceException;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.MediaProxy;
import javax.media.NoDataSourceException;
import net.sf.fmj.media.protocol.merge.DataSource;
import net.sf.fmj.utility.LoggerSingleton;

public class Handler implements MediaProxy {
    private static final Logger logger = LoggerSingleton.logger;
    private DataSource source;

    public javax.media.protocol.DataSource getDataSource() throws IOException, NoDataSourceException {
        try {
            String remainder = this.source.getLocator().getRemainder();
            if (remainder.length() < 3) {
                throw new NoDataSourceException("URL is too short to contain start char, end char, and at least 1 embedded URL");
            }
            String[] urlComponents = remainder.substring(1, remainder.length() - 1).split("\\" + ("" + remainder.charAt(remainder.length() - 1)) + "\\" + ("" + remainder.charAt(0)));
            if (urlComponents.length == 0) {
                throw new NoDataSourceException("No URLs embedded within URL: " + this.source.getLocator());
            }
            javax.media.protocol.DataSource[] dataSourceComponents = new javax.media.protocol.DataSource[urlComponents.length];
            for (int i = 0; i < urlComponents.length; i++) {
                dataSourceComponents[i] = Manager.createDataSource(new MediaLocator(urlComponents[i]));
            }
            return Manager.createMergingDataSource(dataSourceComponents);
        } catch (IncompatibleSourceException e) {
            logger.log(Level.WARNING, "" + e, e);
            throw new NoDataSourceException("" + e);
        } catch (NoDataSourceException e2) {
            logger.log(Level.WARNING, "" + e2, e2);
            throw e2;
        }
    }

    public void setSource(javax.media.protocol.DataSource source) throws IOException, IncompatibleSourceException {
        if (source instanceof DataSource) {
            this.source = (DataSource) source;
            return;
        }
        throw new IncompatibleSourceException();
    }
}
