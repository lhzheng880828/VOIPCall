package org.jivesoftware.smackx.pubsub.util;

import java.io.StringReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.jitsi.gov.nist.core.Separators;
import org.jitsi.gov.nist.javax.sip.header.ims.AuthorizationHeaderIms;

public class XmlUtils {
    public static void prettyPrint(String header, String xml) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty("indent", AuthorizationHeaderIms.YES);
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "3");
            if (header != null) {
                xml = "\n<" + header + Separators.GREATER_THAN + xml + "</" + header + '>';
            }
            transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(System.out));
        } catch (Exception e) {
            System.out.println("Something wrong with xml in \n---------------\n" + xml + "\n---------------");
            e.printStackTrace();
        }
    }

    public static void appendAttribute(StringBuilder builder, String att, String value) {
        builder.append(Separators.SP);
        builder.append(att);
        builder.append("='");
        builder.append(value);
        builder.append(Separators.QUOTE);
    }
}
