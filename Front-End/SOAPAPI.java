package soap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import org.json.JSONArray;
import org.json.JSONObject;
//import org.json.simple.JSONValue;
public class SOAPAPI {

    private Map<String, Object> properties;


    public SOAPAPI(Map<String, Object> prop) {
        this.properties = prop;

    }

    /**
     * @param properties
     *            service = [service name] requestFile = [file name to be used
     *            for sending the SOAP request
     */
    public Map<String, Object> execute(String stepName,
                                       Map<String, Object> parameters) throws Exception {

        Map<String, Object> result = new HashMap<String, Object>();

        // SOAP request send

        // Get all properties from Step Implementation
        String namespace = (String) properties.get("namespace");
        String wsdlStr = (String) properties.get("wsdl");
        String url = (String) properties.get("url");

        // Get dynamic information from parameters
        String serviceStr = (String) parameters.get("service");
        String portStr = (String) parameters.get("port");

        try {
            // Create Service and Port
            QName serviceQName = new QName(namespace, serviceStr);
            URL wsdl = new URL(wsdlStr);
            Service service = Service.create(wsdl, serviceQName);
            QName portQName = new QName(namespace, portStr);

            Dispatch<SOAPMessage> dispatch = null;
            dispatch = service.createDispatch(portQName, SOAPMessage.class,
                    Service.Mode.MESSAGE);
            SOAPMessage request = makeSOAPMessage(parameters);
            System.out.println("Request to be sent is as below : ");
            //System.out.println(getString(request));
            prettyPrintXML(getString(request));

            SOAPMessage response = dispatch.invoke(request);
            String responseXML = getString(response);
            String responseXMLOnly = getXMLString(response);
            result.put("response", responseXMLOnly);
            System.out.println("Response received is as below : ");
            //System.out.println(responseXML);
            //prettyPrintXML(responseXML);
        } catch (Exception e) {
            Exception ex = new Exception("ERROR while executing SOAP Step : " + stepName);
            ex.initCause(e);
            throw ex;
        }

        return result;
    }

    private String getXMLString(SOAPMessage message) throws SOAPException,
            IOException {
        String result = "";
        if (message.countAttachments() > 0) {
            StringWriter sw = new StringWriter();
            try {
                TransformerFactory
                        .newInstance()
                        .newTransformer()
                        .transform(
                                new DOMSource(message.getSOAPPart()
                                        .getEnvelope()), new StreamResult(sw));
            } catch (TransformerException e) {
                throw new RuntimeException(e);
            }
            result = sw.toString();
            System.out.println("Only the Envelope : " + result);
        } else
            result = getString(message);
        return result;
    }

    private String getString(SOAPMessage message) throws SOAPException,
            IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        message.writeTo(baos);
        return baos.toString();
    }

    private SOAPMessage makeSOAPMessage(Map<String, Object> parameters)
            throws Exception {

        try {
            String actualRequestXML = (String) parameters.get("message");

            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage message = factory.createMessage(null,
                    new StringBufferInputStream(actualRequestXML));

            message.saveChanges();
            return message;
        } catch (Exception e) {
            Exception ex = new Exception("Not Able to Form SOAPMessage");
            ex.initCause(e);
            throw ex;
        }
    }

    public static void main(String[] a) throws Exception {
        String regNo = "20120056873";
        JSONObject res=getDemoDetails(regNo);

    }

    public static JSONObject getDemoDetails(String regNo) throws Exception {
        Map prop = new HashMap();
        prop.put("namespace","http://ServiceContainer/");
        prop.put("wsdl","http://10.11.3.18:80/portalws/NIMHANSServiceContainerExtra?wsdl");
        prop.put("url","http://10.11.3.18:80/portalws");

        SOAPAPI api = new SOAPAPI(prop);

        // http://10.5.1.0/dsm/dischargeSummary.jsp?regno=34234328748


        Map parameters = new HashMap();
        parameters.put("service","NIMHANSServiceContainerExtra");
        parameters.put("port","NIMHANSServiceContainerExtraPort");

        String requestMessage = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                + "xmlns:ser=\"http://ServiceContainer/\">  <soapenv:Header/>"
                + "<soapenv:Body><ser:getPatDetailsByRegno><in_token_str>dG9rZW5Ad2ViQGFwcG9pbnQjbmlj</in_token_str>"
                + "<in_reg_no>" + regNo  + "</in_reg_no><in_hos_id></in_hos_id></ser:getPatDetailsByRegno>"
                + "</soapenv:Body></soapenv:Envelope>";

        parameters.put("message", requestMessage);

        Map result = api.execute("Discharge Summary", parameters);
        String response = (String) result.get("response");
        prettyPrintXML(response);

        int start = response.indexOf("<return>");
        int end = response.indexOf("</return>");

        String resultStr = response.substring(start + 8, end);

        //System.out.println("Extracted Result : " + result);

        JSONObject jsonObject = new JSONObject(resultStr);
        JSONArray array = jsonObject.getJSONArray("data");
        JSONObject patient = (JSONObject) array.get(0);



        return patient;

    }

    public static void prettyPrintXML(String str) {

        // Instantiate transformer input
        Source xmlInput = new StreamSource(new StringReader(str));
        StreamResult xmlOutput = new StreamResult(new StringWriter());

        // Configure transformer
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.transform(xmlInput, xmlOutput);

            // Print the pretty XML
            System.out.println(xmlOutput.getWriter().toString());
        } catch (Exception e) {
            System.out.println("Warning: Not able to pretty print the response. So printing as-is");
            System.out.println(str);
        }
    }

}
