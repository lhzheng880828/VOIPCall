package net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules;

import net.java.sip.communicator.impl.protocol.sip.xcap.PresRulesClient;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.XmlUtils;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy.ActionsType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy.CommonPolicyParser;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.commonpolicy.TransformationsType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules.ProvideDevicePermissionType.AllDevicesType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules.ProvideDevicePermissionType.DeviceIdType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules.ProvidePersonPermissionType.AllPersonsType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules.ProvideServicePermissionType.AllServicesType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules.ProvideServicePermissionType.ServiceUriSchemeType;
import net.java.sip.communicator.impl.protocol.sip.xcap.model.presrules.ProvideServicePermissionType.ServiceUriType;
import org.jitsi.util.xml.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class PresRulesParser {
    private static String CLASS_ELEMENT = "class";
    private static String NAMESPACE = PresRulesClient.NAMESPACE;
    private static String OCCURRENCE_ID_ELEMENT = "occurrence-id";
    private static String PROVIDE_DEVICES_ALL_ELEMENT = "all-devices";
    private static String PROVIDE_DEVICES_DEVICEID_ELEMENT = "deviceID";
    private static String PROVIDE_DEVICES_ELEMENT = "provide-devices";
    private static String PROVIDE_PERSONS_ALL_ELEMENT = "all-persons";
    private static String PROVIDE_PERSONS_ELEMENT = "provide-persons";
    private static String PROVIDE_SERVICES_ALL_ELEMENT = "all-services";
    private static String PROVIDE_SERVICES_ELEMENT = "provide-services";
    private static String PROVIDE_SERVICES_SERBICE_URI_ELEMENT = "service-uri";
    private static String PROVIDE_SERVICES_SERBICE_URI_SCHEME_ELEMENT = "service-uri-scheme";
    private static String SUBHANDLING_ELEMENT = "sub-handling";

    public static ActionsType actionsFromElement(Element element) throws Exception {
        ActionsType actions = new ActionsType();
        if (CommonPolicyParser.NAMESPACE.equals(XMLUtils.getNamespaceUri(element)) && CommonPolicyParser.ACTIONS_ELEMENT.equals(element.getLocalName())) {
            String namespaceUri;
            NamedNodeMap attributes = element.getAttributes();
            int i = 0;
            while (i < attributes.getLength()) {
                namespaceUri = XMLUtils.getNamespaceUri((Attr) attributes.item(i));
                if (namespaceUri == null) {
                    throw new Exception("actions element is invalid");
                } else if (XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    i++;
                } else {
                    throw new Exception("actions element is invalid");
                }
            }
            SubHandlingType subHandling = null;
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element childElement = (Element) node;
                    String localName = childElement.getLocalName();
                    namespaceUri = XMLUtils.getNamespaceUri(childElement);
                    if (CommonPolicyParser.NAMESPACE.equals(namespaceUri)) {
                        throw new Exception("actions element is invalid");
                    } else if (!NAMESPACE.equals(namespaceUri)) {
                        actions.getAny().add(childElement);
                    } else if (SUBHANDLING_ELEMENT.equals(localName) && subHandling == null) {
                        subHandling = SubHandlingType.fromString(childElement.getTextContent().toLowerCase());
                    } else {
                        throw new Exception("actions element is invalid");
                    }
                }
            }
            actions.setSubHandling(subHandling);
            return actions;
        }
        throw new Exception("actions element is invalid");
    }

    public static Element elementFromActions(Document document, ActionsType actions) throws Exception {
        Element element = document.createElementNS(CommonPolicyParser.NAMESPACE, CommonPolicyParser.ACTIONS_ELEMENT);
        if (actions.getSubHandling() != null) {
            Element subHandlingElement = document.createElementNS(NAMESPACE, SUBHANDLING_ELEMENT);
            subHandlingElement.setTextContent(actions.getSubHandling().value());
            element.appendChild(subHandlingElement);
        }
        XmlUtils.processAny(element, actions.getAny());
        return element;
    }

    public static TransformationsType transformationsFromElement(Element element) throws Exception {
        TransformationsType transfomations = new TransformationsType();
        if (CommonPolicyParser.NAMESPACE.equals(XMLUtils.getNamespaceUri(element)) && CommonPolicyParser.TRANSFORMATIONS_ELEMENT.equals(element.getLocalName())) {
            String namespaceUri;
            NamedNodeMap attributes = element.getAttributes();
            int i = 0;
            while (i < attributes.getLength()) {
                namespaceUri = XMLUtils.getNamespaceUri((Attr) attributes.item(i));
                if (namespaceUri == null) {
                    throw new Exception("transfomations element is invalid");
                } else if (XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    i++;
                } else {
                    throw new Exception("transfomations element is invalid");
                }
            }
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element childElement = (Element) node;
                    String localName = childElement.getLocalName();
                    namespaceUri = XMLUtils.getNamespaceUri(childElement);
                    if (CommonPolicyParser.NAMESPACE.equals(namespaceUri)) {
                        throw new Exception("transfomations element is invalid");
                    } else if (!NAMESPACE.equals(namespaceUri)) {
                        transfomations.getAny().add(childElement);
                    } else if (PROVIDE_DEVICES_ELEMENT.equals(localName)) {
                        transfomations.setDevicePermission(devicePermissionFromElement(childElement));
                    } else if (PROVIDE_SERVICES_ELEMENT.equals(localName)) {
                        transfomations.setServicePermission(servicePermissionFromElement(childElement));
                    } else if (PROVIDE_PERSONS_ELEMENT.equals(localName)) {
                        transfomations.setPersonPermission(personPermissionFromElement(childElement));
                    } else {
                        transfomations.getAny().add(childElement);
                    }
                }
            }
            return transfomations;
        }
        throw new Exception("transfomations element is invalid");
    }

    public static Element elementFromTransfomations(Document document, TransformationsType transformations) throws Exception {
        Element element = document.createElementNS(CommonPolicyParser.NAMESPACE, CommonPolicyParser.TRANSFORMATIONS_ELEMENT);
        if (transformations.getDevicePermission() != null) {
            element.appendChild(elementFromDevicePermission(document, transformations.getDevicePermission()));
        }
        if (transformations.getPersonPermission() != null) {
            element.appendChild(elementFromPersonPermission(document, transformations.getPersonPermission()));
        }
        if (transformations.getServicePermission() != null) {
            element.appendChild(elementFromServicePermission(document, transformations.getServicePermission()));
        }
        XmlUtils.processAny(element, transformations.getAny());
        return element;
    }

    private static ProvideServicePermissionType servicePermissionFromElement(Element element) throws Exception {
        ProvideServicePermissionType servicePermission = new ProvideServicePermissionType();
        if (NAMESPACE.equals(XMLUtils.getNamespaceUri(element)) && PROVIDE_SERVICES_ELEMENT.equals(element.getLocalName())) {
            NamedNodeMap attributes = element.getAttributes();
            int i = 0;
            while (i < attributes.getLength()) {
                String namespaceUri = XMLUtils.getNamespaceUri((Attr) attributes.item(i));
                if (namespaceUri == null) {
                    throw new Exception("provide-services element is invalid");
                } else if (XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    i++;
                } else {
                    throw new Exception("provide-services element is invalid");
                }
            }
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element childElement = (Element) node;
                    String localName = childElement.getLocalName();
                    if (!NAMESPACE.equals(XMLUtils.getNamespaceUri(childElement))) {
                        servicePermission.getAny().add(childElement);
                    } else if (PROVIDE_SERVICES_ALL_ELEMENT.equals(localName)) {
                        if (XMLUtils.hasChildElements(childElement)) {
                            throw new Exception("all-services element is invalid");
                        }
                        servicePermission.setAllServices(new AllServicesType());
                    } else if (PROVIDE_SERVICES_SERBICE_URI_ELEMENT.equals(localName)) {
                        if (XMLUtils.hasChildElements(childElement)) {
                            throw new Exception("service-uri element is invalid");
                        }
                        servicePermission.getServiceUriList().add(new ServiceUriType(childElement.getTextContent()));
                    } else if (PROVIDE_SERVICES_SERBICE_URI_SCHEME_ELEMENT.equals(localName)) {
                        if (XMLUtils.hasChildElements(childElement)) {
                            throw new Exception("service-scheme-uri element is invalid");
                        }
                        servicePermission.getServiceUriSchemeList().add(new ServiceUriSchemeType(childElement.getTextContent()));
                    } else if (OCCURRENCE_ID_ELEMENT.equals(localName)) {
                        if (XMLUtils.hasChildElements(childElement)) {
                            throw new Exception("occurrence-id element is invalid");
                        }
                        servicePermission.getOccurrences().add(new OccurrenceIdType(childElement.getTextContent()));
                    } else if (!CLASS_ELEMENT.equals(localName)) {
                        throw new Exception("provide-services element is invalid");
                    } else if (XMLUtils.hasChildElements(childElement)) {
                        throw new Exception("class element is invalid");
                    } else {
                        servicePermission.getClasses().add(new ClassType(childElement.getTextContent()));
                    }
                }
            }
            return servicePermission;
        }
        throw new Exception("provide-services element is invalid");
    }

    public static Element elementFromServicePermission(Document document, ProvideServicePermissionType serviceService) throws Exception {
        Element element = document.createElementNS(NAMESPACE, PROVIDE_SERVICES_ELEMENT);
        if (serviceService.getAllServices() != null) {
            element.appendChild(document.createElementNS(NAMESPACE, PROVIDE_SERVICES_ALL_ELEMENT));
        } else {
            Element serviceUriElement;
            for (ServiceUriType serviceUri : serviceService.getServiceUriList()) {
                serviceUriElement = document.createElementNS(NAMESPACE, PROVIDE_SERVICES_SERBICE_URI_ELEMENT);
                serviceUriElement.setTextContent(serviceUri.getValue());
                element.appendChild(serviceUriElement);
            }
            for (ServiceUriSchemeType serviceUriSheme : serviceService.getServiceUriSchemeList()) {
                serviceUriElement = document.createElementNS(NAMESPACE, PROVIDE_SERVICES_SERBICE_URI_SCHEME_ELEMENT);
                serviceUriElement.setTextContent(serviceUriSheme.getValue());
                element.appendChild(serviceUriElement);
            }
            for (ClassType classType : serviceService.getClasses()) {
                Element classElement = document.createElementNS(NAMESPACE, CLASS_ELEMENT);
                classElement.setTextContent(classType.getValue());
                element.appendChild(classElement);
            }
            for (OccurrenceIdType occurrence : serviceService.getOccurrences()) {
                Element occurrenceElement = document.createElementNS(NAMESPACE, OCCURRENCE_ID_ELEMENT);
                occurrenceElement.setTextContent(occurrence.getValue());
                element.appendChild(occurrenceElement);
            }
        }
        XmlUtils.processAny(element, serviceService.getAny());
        return element;
    }

    private static ProvideDevicePermissionType devicePermissionFromElement(Element element) throws Exception {
        ProvideDevicePermissionType devicePermission = new ProvideDevicePermissionType();
        if (NAMESPACE.equals(XMLUtils.getNamespaceUri(element)) && PROVIDE_DEVICES_ELEMENT.equals(element.getLocalName())) {
            NamedNodeMap attributes = element.getAttributes();
            int i = 0;
            while (i < attributes.getLength()) {
                String namespaceUri = XMLUtils.getNamespaceUri((Attr) attributes.item(i));
                if (namespaceUri == null) {
                    throw new Exception("provide-devices element is invalid");
                } else if (XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    i++;
                } else {
                    throw new Exception("provide-devices element is invalid");
                }
            }
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element childElement = (Element) node;
                    String localName = childElement.getLocalName();
                    if (!NAMESPACE.equals(XMLUtils.getNamespaceUri(childElement))) {
                        devicePermission.getAny().add(childElement);
                    } else if (PROVIDE_DEVICES_ALL_ELEMENT.equals(localName)) {
                        if (XMLUtils.hasChildElements(childElement)) {
                            throw new Exception("all-devices element is invalid");
                        }
                        devicePermission.setAllDevices(new AllDevicesType());
                    } else if (PROVIDE_DEVICES_DEVICEID_ELEMENT.equals(localName)) {
                        if (XMLUtils.hasChildElements(childElement)) {
                            throw new Exception("deviceID element is invalid");
                        }
                        devicePermission.getDevices().add(new DeviceIdType(childElement.getTextContent()));
                    } else if (OCCURRENCE_ID_ELEMENT.equals(localName)) {
                        if (XMLUtils.hasChildElements(childElement)) {
                            throw new Exception("occurrence-id element is invalid");
                        }
                        devicePermission.getOccurrences().add(new OccurrenceIdType(childElement.getTextContent()));
                    } else if (!CLASS_ELEMENT.equals(localName)) {
                        throw new Exception("provide-devices element is invalid");
                    } else if (XMLUtils.hasChildElements(childElement)) {
                        throw new Exception("class element is invalid");
                    } else {
                        devicePermission.getClasses().add(new ClassType(childElement.getTextContent()));
                    }
                }
            }
            return devicePermission;
        }
        throw new Exception("provide-devices element is invalid");
    }

    public static Element elementFromDevicePermission(Document document, ProvideDevicePermissionType devicePermission) throws Exception {
        Element element = document.createElementNS(NAMESPACE, PROVIDE_DEVICES_ELEMENT);
        if (devicePermission.getAllDevices() != null) {
            element.appendChild(document.createElementNS(NAMESPACE, PROVIDE_DEVICES_ALL_ELEMENT));
        } else {
            for (DeviceIdType device : devicePermission.getDevices()) {
                Element deviceElement = document.createElementNS(NAMESPACE, PROVIDE_DEVICES_DEVICEID_ELEMENT);
                deviceElement.setTextContent(device.getValue());
                element.appendChild(deviceElement);
            }
            for (ClassType classType : devicePermission.getClasses()) {
                Element classElement = document.createElementNS(NAMESPACE, CLASS_ELEMENT);
                classElement.setTextContent(classType.getValue());
                element.appendChild(classElement);
            }
            for (OccurrenceIdType occurrence : devicePermission.getOccurrences()) {
                Element occurrenceElement = document.createElementNS(NAMESPACE, OCCURRENCE_ID_ELEMENT);
                occurrenceElement.setTextContent(occurrence.getValue());
                element.appendChild(occurrenceElement);
            }
        }
        XmlUtils.processAny(element, devicePermission.getAny());
        return element;
    }

    private static ProvidePersonPermissionType personPermissionFromElement(Element element) throws Exception {
        ProvidePersonPermissionType personPermission = new ProvidePersonPermissionType();
        if (NAMESPACE.equals(XMLUtils.getNamespaceUri(element)) && PROVIDE_PERSONS_ELEMENT.equals(element.getLocalName())) {
            NamedNodeMap attributes = element.getAttributes();
            int i = 0;
            while (i < attributes.getLength()) {
                String namespaceUri = XMLUtils.getNamespaceUri((Attr) attributes.item(i));
                if (namespaceUri == null) {
                    throw new Exception("provide-persons element is invalid");
                } else if (XMLUtils.isStandartXmlNamespace(namespaceUri)) {
                    i++;
                } else {
                    throw new Exception("provide-persons element is invalid");
                }
            }
            NodeList childNodes = element.getChildNodes();
            for (i = 0; i < childNodes.getLength(); i++) {
                Node node = childNodes.item(i);
                if (node.getNodeType() == (short) 1) {
                    Element childElement = (Element) node;
                    String localName = childElement.getLocalName();
                    if (!NAMESPACE.equals(XMLUtils.getNamespaceUri(childElement))) {
                        personPermission.getAny().add(childElement);
                    } else if (PROVIDE_PERSONS_ALL_ELEMENT.equals(localName)) {
                        if (XMLUtils.hasChildElements(childElement)) {
                            throw new Exception("all-persons element is invalid");
                        }
                        personPermission.setAllPersons(new AllPersonsType());
                    } else if (OCCURRENCE_ID_ELEMENT.equals(localName)) {
                        if (XMLUtils.hasChildElements(childElement)) {
                            throw new Exception("occurrence-id element is invalid");
                        }
                        personPermission.getOccurrences().add(new OccurrenceIdType(childElement.getTextContent()));
                    } else if (!CLASS_ELEMENT.equals(localName)) {
                        throw new Exception("provide-persons element is invalid");
                    } else if (XMLUtils.hasChildElements(childElement)) {
                        throw new Exception("class element is invalid");
                    } else {
                        personPermission.getClasses().add(new ClassType(childElement.getTextContent()));
                    }
                }
            }
            return personPermission;
        }
        throw new Exception("provide-persons element is invalid");
    }

    public static Element elementFromPersonPermission(Document document, ProvidePersonPermissionType personPermission) throws Exception {
        Element element = document.createElementNS(NAMESPACE, PROVIDE_PERSONS_ELEMENT);
        if (personPermission.getAllPersons() != null) {
            element.appendChild(document.createElementNS(NAMESPACE, PROVIDE_PERSONS_ALL_ELEMENT));
        } else {
            for (ClassType classType : personPermission.getClasses()) {
                Element classElement = document.createElementNS(NAMESPACE, CLASS_ELEMENT);
                classElement.setTextContent(classType.getValue());
                element.appendChild(classElement);
            }
            for (OccurrenceIdType occurrence : personPermission.getOccurrences()) {
                Element occurrenceElement = document.createElementNS(NAMESPACE, OCCURRENCE_ID_ELEMENT);
                occurrenceElement.setTextContent(occurrence.getValue());
                element.appendChild(occurrenceElement);
            }
        }
        XmlUtils.processAny(element, personPermission.getAny());
        return element;
    }
}
