package net.sf.fmj.utility;

import com.lti.utils.StringUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import javax.media.Format;

public class SerializationUtils {
    public static Format deserialize(String s) throws IOException, ClassNotFoundException {
        ByteArrayInputStream inbuf = new ByteArrayInputStream(StringUtils.hexStringToByteArray(s));
        ObjectInputStream input = new ObjectInputStream(inbuf);
        Object oRead = input.readObject();
        input.close();
        inbuf.close();
        return (Format) oRead;
    }

    public static String serialize(Format f) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream(buffer);
        output.writeObject(f);
        output.close();
        buffer.close();
        return StringUtils.byteArrayToHexString(buffer.toByteArray());
    }
}
