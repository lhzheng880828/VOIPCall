package net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy;

import java.util.ArrayList;
import java.util.List;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ParameterPacketExtension;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.ParsingException;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.XmlUtils;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules.PresRulesParser;
import org.jitsi.util.StringUtils;
import org.jitsi.util.xml.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class CommonPolicyParser {
    public static String ACTIONS_ELEMENT = "actions";
    public static String CONDITIONS_ELEMENT = "conditions";
    public static String EXCEPT_DOMAIN_ATTR = "domain";
    public static String EXCEPT_ELEMENT = "except";
    public static String EXCEPT_ID_ATTR = "id";
    public static String IDENTITY_ELEMENT = "identity";
    public static String MANY_DOMAIN_ATTR = "domain";
    public static String MANY_ELEMENT = "many";
    public static String NAMESPACE = "urn:ietf:params:xml:ns:common-policy";
    public static String ONE_ELEMENT = "one";
    public static String ONE_ID_ATTR = "id";
    public static String RULESET_ELEMENT = "ruleset";
    public static String RULE_ELEMENT = "rule";
    public static String RULE_ID_ATTR = "id";
    public static String SPHERE_ELEMENT = "sphere";
    public static String SPHERE_VALUE_ATTR = ParameterPacketExtension.VALUE_ATTR_NAME;
    public static String TRANSFORMATIONS_ELEMENT = "transformations";
    public static String VALIDITY_ELEMENT = "validity";
    public static String VALIDITY_FROM_ELEMENT = "from";
    public static String VALIDITY_UNTIL_ELEMENT = "until";

    private CommonPolicyParser() {
    }

    public static RulesetType fromXml(String xml) throws ParsingException {
        if (StringUtils.isNullOrEmpty(xml)) {
            throw new IllegalArgumentException("XML cannot be null or empty");
        }
        try {
            RulesetType ruleset = new RulesetType();
            Element rulesetElement = XMLUtils.createDocument(xml).getDocumentElement();
            if (NAMESPACE.equals(XMLUtils.getNamespaceUri(rulesetElement)) && RULESET_ELEMENT.equals(rulesetElement.getLocalName())) {
                NamedNodeMap attributes = rulesetElement.getAttributes();
                int i = 0;
                while (i < attributes.getLength()) {
                    String namespaceUri = XMLUtils.getNamespaceUri((Attr) attributes.item(i));
                    if (namespaceUri == null) {
                        throw new Exception("ruleset element is invalid");
                    } else if (XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                        i++;
                    } else {
                        throw new Exception("ruleset element is invalid");
                    }
                }
                NodeList childNodes = rulesetElement.getChildNodes();
                for (i = 0; i < childNodes.getLength(); i++) {
                    Node node = childNodes.item(i);
                    if (node.getNodeType() == (short) 1) {
                        ruleset.getRules().add(ruleFromElement((Element) node));
                    }
                }
                return ruleset;
            }
            throw new Exception("Document doesn't contain ruleset element");
        } catch (Exception ex) {
            throw new ParsingException(ex);
        }
    }

    public static String toXml(RulesetType ruleset) throws ParsingException {
        if (ruleset == null) {
            throw new IllegalArgumentException("ruleset cannot be null");
        }
        try {
            Document document = XMLUtils.createDocument();
            Element rulesetElement = document.createElementNS(NAMESPACE, RULESET_ELEMENT);
            for (RuleType rule : ruleset.getRules()) {
                rulesetElement.appendChild(elementFromRule(document, rule));
            }
            document.appendChild(rulesetElement);
            return XMLUtils.createXml(document);
        } catch (Exception ex) {
            throw new ParsingException(ex);
        }
    }

    private static RuleType ruleFromElement(Element element) throws Exception {
        RuleType rule = new RuleType();
        if (NAMESPACE.equals(XMLUtils.getNamespaceUri(element)) && RULE_ELEMENT.equals(element.getLocalName())) {
            int i;
            String id = null;
            NamedNodeMap attributes = element.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                String namespaceUri = XMLUtils.getNamespaceUri(attribute);
                if (namespaceUri == null) {
                    throw new Exception("rule element is invalid");
                }
                if (!XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    if (NAMESPACE.equals(namespaceUri) && RULE_ID_ATTR.equals(attribute.getLocalName()) && id == null) {
                        id = attribute.getValue();
                    } else {
                        throw new Exception("rule element is invalid");
                    }
                }
            }
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element childElement = (Element) node;
                    String localName = childElement.getLocalName();
                    if (!NAMESPACE.equals(XMLUtils.getNamespaceUri(childElement))) {
                        throw new Exception("rule element is invalid");
                    } else if (CONDITIONS_ELEMENT.equals(localName)) {
                        rule.setConditions(conditionsFromElement(childElement));
                    } else if (ACTIONS_ELEMENT.equals(localName)) {
                        rule.setActions(PresRulesParser.actionsFromElement(childElement));
                    } else if (TRANSFORMATIONS_ELEMENT.equals(localName)) {
                        rule.setTransformations(PresRulesParser.transformationsFromElement(childElement));
                    } else {
                        throw new Exception("rule element is invalid");
                    }
                }
            }
            if (id == null) {
                throw new Exception("rule id attribute is missed");
            }
            rule.setId(id);
            return rule;
        }
        throw new Exception("rule element is invalid");
    }

    private static Element elementFromRule(Document document, RuleType rule) throws Exception {
        Element element = document.createElementNS(NAMESPACE, RULE_ELEMENT);
        if (StringUtils.isNullOrEmpty(rule.getId())) {
            throw new Exception("rule element is invalid");
        }
        element.setAttribute(RULE_ID_ATTR, rule.getId());
        if (rule.getConditions() != null) {
            element.appendChild(elementFromConditions(document, rule.getConditions()));
        }
        if (rule.getActions() != null) {
            element.appendChild(PresRulesParser.elementFromActions(document, rule.getActions()));
        }
        if (rule.getTransformations() != null) {
            element.appendChild(PresRulesParser.elementFromTransfomations(document, rule.getTransformations()));
        }
        return element;
    }

    private static ConditionsType conditionsFromElement(Element element) throws Exception {
        ConditionsType conditions = new ConditionsType();
        if (NAMESPACE.equals(XMLUtils.getNamespaceUri(element)) && CONDITIONS_ELEMENT.equals(element.getLocalName())) {
            NamedNodeMap attributes = element.getAttributes();
            int i = 0;
            while (i < attributes.getLength()) {
                String namespaceUri = XMLUtils.getNamespaceUri((Attr) attributes.item(i));
                if (namespaceUri == null) {
                    throw new Exception("conditions element is invalid");
                } else if (XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    i++;
                } else {
                    throw new Exception("conditions element is invalid");
                }
            }
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element childElement = (Element) node;
                    String localName = childElement.getLocalName();
                    if (!NAMESPACE.equals(XMLUtils.getNamespaceUri(childElement))) {
                        conditions.getAny().add(childElement);
                    } else if (IDENTITY_ELEMENT.equals(localName)) {
                        conditions.getIdentities().add(identityFromElement(childElement));
                    } else if (SPHERE_ELEMENT.equals(localName)) {
                        conditions.getSpheres().add(sphereFromElement(childElement));
                    } else if (VALIDITY_ELEMENT.equals(localName)) {
                        conditions.getValidities().add(validityFromElement(childElement));
                    } else {
                        throw new Exception("conditions element is invalid");
                    }
                }
            }
            return conditions;
        }
        throw new Exception("conditions element is invalid");
    }

    private static Element elementFromConditions(Document document, ConditionsType conditions) throws Exception {
        Element element = document.createElementNS(NAMESPACE, CONDITIONS_ELEMENT);
        for (IdentityType identity : conditions.getIdentities()) {
            element.appendChild(elementFromIdentity(document, identity));
        }
        for (SphereType sphere : conditions.getSpheres()) {
            element.appendChild(elementFromSphere(document, sphere));
        }
        for (ValidityType validity : conditions.getValidities()) {
            element.appendChild(elementFromValidity(document, validity));
        }
        XmlUtils.processAny(element, conditions.getAny());
        return element;
    }

    private static ValidityType validityFromElement(Element element) throws Exception {
        ValidityType validity = new ValidityType();
        if (NAMESPACE.equals(XMLUtils.getNamespaceUri(element)) && VALIDITY_ELEMENT.equals(element.getLocalName())) {
            NamedNodeMap attributes = element.getAttributes();
            int i = 0;
            while (i < attributes.getLength()) {
                String namespaceUri = XMLUtils.getNamespaceUri((Attr) attributes.item(i));
                if (namespaceUri == null) {
                    throw new Exception("sphere element is invalid");
                } else if (XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    i++;
                } else {
                    throw new Exception("validity element is invalid");
                }
            }
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element childElement = (Element) node;
                    String localName = childElement.getLocalName();
                    if (!NAMESPACE.equals(XMLUtils.getNamespaceUri(childElement))) {
                        throw new Exception("sphere element is invalid");
                    } else if (VALIDITY_FROM_ELEMENT.equals(localName)) {
                        validity.getFromList().add(childElement.getTextContent());
                    } else if (VALIDITY_UNTIL_ELEMENT.equals(localName)) {
                        validity.getUntilList().add(childElement.getTextContent());
                    } else {
                        throw new Exception("sphere element is invalid");
                    }
                }
            }
            return validity;
        }
        throw new Exception("validity element is invalid");
    }

    private static Element elementFromValidity(Document document, ValidityType validity) throws Exception {
        Element element = document.createElementNS(NAMESPACE, VALIDITY_ELEMENT);
        for (String from : validity.getFromList()) {
            Element fromElement = document.createElementNS(NAMESPACE, VALIDITY_FROM_ELEMENT);
            fromElement.setTextContent(from);
            element.appendChild(fromElement);
        }
        for (String until : validity.getUntilList()) {
            Element untilElement = document.createElementNS(NAMESPACE, VALIDITY_UNTIL_ELEMENT);
            untilElement.setTextContent(until);
            element.appendChild(untilElement);
        }
        return element;
    }

    private static SphereType sphereFromElement(Element element) throws Exception {
        SphereType sphere = new SphereType();
        if (NAMESPACE.equals(XMLUtils.getNamespaceUri(element)) && SPHERE_ELEMENT.equals(element.getLocalName())) {
            int i;
            String value = null;
            NamedNodeMap attributes = element.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                String namespaceUri = XMLUtils.getNamespaceUri(attribute);
                if (namespaceUri == null) {
                    throw new Exception("sphere element is invalid");
                }
                if (!XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    if (NAMESPACE.equals(namespaceUri) && SPHERE_VALUE_ATTR.equals(attribute.getLocalName()) && value == null) {
                        value = attribute.getValue();
                    } else {
                        throw new Exception("sphere element is invalid");
                    }
                }
            }
            NodeList childNodes = element.getChildNodes();
            i = 0;
            while (i < childNodes.getLength()) {
                if (childNodes.item(i).getNodeType() != (short) 1) {
                    i++;
                } else {
                    throw new Exception("sphere element is invalid");
                }
            }
            if (value == null) {
                throw new Exception("sphere value attribute is missed");
            }
            sphere.setValue(value);
            return sphere;
        }
        throw new Exception("sphere element is invalid");
    }

    private static Element elementFromSphere(Document document, SphereType sphere) throws Exception {
        Element element = document.createElementNS(NAMESPACE, SPHERE_ELEMENT);
        if (StringUtils.isNullOrEmpty(sphere.getValue())) {
            throw new Exception("sphere value attribute is missed");
        }
        element.setAttribute(SPHERE_VALUE_ATTR, sphere.getValue());
        return element;
    }

    private static IdentityType identityFromElement(Element element) throws Exception {
        IdentityType identity = new IdentityType();
        if (NAMESPACE.equals(XMLUtils.getNamespaceUri(element)) && IDENTITY_ELEMENT.equals(element.getLocalName())) {
            NamedNodeMap attributes = element.getAttributes();
            int i = 0;
            while (i < attributes.getLength()) {
                String namespaceUri = XMLUtils.getNamespaceUri((Attr) attributes.item(i));
                if (namespaceUri == null) {
                    throw new Exception("identity element is invalid");
                } else if (XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    i++;
                } else {
                    throw new Exception("identity element is invalid");
                }
            }
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element childElement = (Element) node;
                    String localName = childElement.getLocalName();
                    if (!NAMESPACE.equals(XMLUtils.getNamespaceUri(childElement))) {
                        identity.getAny().add(childElement);
                    } else if (ONE_ELEMENT.equals(localName)) {
                        identity.getOneList().add(oneFromElement(childElement));
                    } else if (MANY_ELEMENT.equals(localName)) {
                        identity.getManyList().add(manyFromElement(childElement));
                    } else {
                        throw new Exception("identity element is invalid");
                    }
                }
            }
            return identity;
        }
        throw new Exception("identity element is invalid");
    }

    private static Element elementFromIdentity(Document document, IdentityType identity) throws Exception {
        Element element = document.createElementNS(NAMESPACE, IDENTITY_ELEMENT);
        for (OneType one : identity.getOneList()) {
            element.appendChild(elementFromOne(document, one));
        }
        for (ManyType many : identity.getManyList()) {
            element.appendChild(elementFromMany(document, many));
        }
        XmlUtils.processAny(element, identity.getAny());
        return element;
    }

    private static OneType oneFromElement(Element element) throws Exception {
        OneType one = new OneType();
        if (NAMESPACE.equals(XMLUtils.getNamespaceUri(element)) && ONE_ELEMENT.equals(element.getLocalName())) {
            int i;
            String id = null;
            NamedNodeMap attributes = element.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                String namespaceUri = XMLUtils.getNamespaceUri(attribute);
                if (namespaceUri == null) {
                    throw new Exception("one element is invalid");
                }
                if (!XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    if (NAMESPACE.equals(namespaceUri) && ONE_ID_ATTR.equals(attribute.getLocalName()) && id == null) {
                        id = attribute.getValue();
                    } else {
                        throw new Exception("one element is invalid");
                    }
                }
            }
            Element any = null;
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element childElement = (Element) node;
                    if (NAMESPACE.equals(XMLUtils.getNamespaceUri(childElement)) || any != null) {
                        throw new Exception("one element is invalid");
                    }
                    any = childElement;
                }
            }
            if (id == null) {
                throw new Exception("one id attribute is missed");
            }
            one.setId(id);
            one.setAny(any);
            return one;
        }
        throw new Exception("one element is invalid");
    }

    private static Element elementFromOne(Document document, OneType one) throws Exception {
        Element element = document.createElementNS(NAMESPACE, ONE_ELEMENT);
        if (StringUtils.isNullOrEmpty(one.getId())) {
            throw new Exception("one id attribute is missed");
        }
        element.setAttribute(ONE_ID_ATTR, one.getId());
        if (one.getAny() != null) {
            List<Element> any = new ArrayList();
            any.add(one.getAny());
            XmlUtils.processAny(element, any);
        }
        return element;
    }

    private static ManyType manyFromElement(Element element) throws Exception {
        ManyType many = new ManyType();
        if (NAMESPACE.equals(XMLUtils.getNamespaceUri(element)) && MANY_ELEMENT.equals(element.getLocalName())) {
            int i;
            String namespaceUri;
            String domain = null;
            NamedNodeMap attributes = element.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                namespaceUri = XMLUtils.getNamespaceUri(attribute);
                if (namespaceUri == null) {
                    throw new Exception("many element is invalid");
                }
                if (!XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    if (NAMESPACE.equals(namespaceUri) && MANY_DOMAIN_ATTR.equals(attribute.getLocalName()) && domain == null) {
                        domain = attribute.getValue();
                    } else {
                        throw new Exception("many element is invalid");
                    }
                }
            }
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element childElement = (Element) node;
                    namespaceUri = XMLUtils.getNamespaceUri(childElement);
                    String localName = childElement.getLocalName();
                    if (!NAMESPACE.equals(namespaceUri)) {
                        many.getAny().add(childElement);
                    } else if (EXCEPT_ELEMENT.equals(localName)) {
                        many.getExcepts().add(exceptFromElement(childElement));
                    } else {
                        throw new Exception("many element is invalid");
                    }
                }
            }
            many.setDomain(domain);
            return many;
        }
        throw new Exception("many element is invalid");
    }

    private static Element elementFromMany(Document document, ManyType many) throws Exception {
        Element element = document.createElementNS(NAMESPACE, MANY_ELEMENT);
        if (many.getDomain() != null) {
            element.setAttribute(MANY_DOMAIN_ATTR, many.getDomain());
        }
        for (ExceptType except : many.getExcepts()) {
            element.appendChild(elementFromExept(document, except));
        }
        XmlUtils.processAny(element, many.getAny());
        return element;
    }

    private static ExceptType exceptFromElement(Element element) throws Exception {
        ExceptType except = new ExceptType();
        if (NAMESPACE.equals(XMLUtils.getNamespaceUri(element)) && EXCEPT_ELEMENT.equals(element.getLocalName())) {
            int i;
            String id = null;
            String domain = null;
            NamedNodeMap attributes = element.getAttributes();
            for (i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                String namespaceUri = XMLUtils.getNamespaceUri(attribute);
                if (namespaceUri == null) {
                    throw new Exception("except element is invalid");
                }
                if (!XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    if (!NAMESPACE.equals(namespaceUri)) {
                        throw new Exception("except element is invalid");
                    } else if (EXCEPT_ID_ATTR.equals(attribute.getLocalName()) && id == null) {
                        id = attribute.getValue();
                    } else if (EXCEPT_DOMAIN_ATTR.equals(attribute.getLocalName()) && domain == null) {
                        domain = attribute.getValue();
                    } else {
                        throw new Exception("except element is invalid");
                    }
                }
            }
            NodeList childNodes = element.getChildNodes();
            i = 0;
            while (i < childNodes.getLength()) {
                if (childNodes.item(i).getNodeType() != (short) 1) {
                    i++;
                } else {
                    throw new Exception("except element is invalid");
                }
            }
            except.setId(id);
            except.setDomain(domain);
            return except;
        }
        throw new Exception("except element is invalid");
    }

    private static Element elementFromExept(Document document, ExceptType except) throws Exception {
        Element element = document.createElementNS(NAMESPACE, EXCEPT_ELEMENT);
        if (except.getId() != null) {
            element.setAttribute(EXCEPT_ID_ATTR, except.getId());
        }
        if (except.getDomain() != null) {
            element.setAttribute(EXCEPT_DOMAIN_ATTR, except.getDomain());
        }
        return element;
    }
}
