package testbed12;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.n52.wps.server.request.strategy.ReferenceInputStream;

public class SOAPTest {

    public static void main(String[] args) {
        new SOAPTest();
    }

    public SOAPTest(){

        try {
            
            String url = "http://polar.geodacenter.org/geoserver29/testbed12/ows";
            
            String mimeType = "application/soap+xml";
            
            XmlObject soapEnvelope = XmlObject.Factory.parse(SOAPTest.class.getResourceAsStream("GetFeatureSOAP.xml"));

            ReferenceInputStream inputStream = httpPost(url, soapEnvelope.toString(), mimeType);
            
//            XmlObject.Factory.parse(inputStream);
            
            StringBuilder builder = new StringBuilder();
            
            int i = 0;
            
            while((i = inputStream.read()) != -1){
                builder.append((char)i);
            }
            
            String soapString = builder.toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
            
            XmlObject.Factory.parse(soapString);
            
        } catch (XmlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        
    }
    
    /**
     * Make a POST request using mimeType and href
     * 
     * TODO: add support for autoretry, proxy
     */
    private ReferenceInputStream httpPost(final String dataURLString,
            final String body,
            final String mimeType) throws IOException {
        HttpClient backend = new DefaultHttpClient();

        DecompressingHttpClient httpclient = new DecompressingHttpClient(backend);

        HttpPost httppost = new HttpPost(dataURLString);

        if (mimeType != null) {
            httppost.addHeader(new BasicHeader("Content-type", mimeType));
        }

        // set body entity
        HttpEntity postEntity = new StringEntity(body);
        httppost.setEntity(postEntity);

        return processResponse(httpclient.execute(httppost));
    }

    private ReferenceInputStream processResponse(HttpResponse response) throws IOException {

        HttpEntity entity = response.getEntity();
        Header header;

        header = entity.getContentType();
        String mimeType = header == null ? null : header.getValue();

        header = entity.getContentEncoding();
        String encoding = header == null ? null : header.getValue();

        return new ReferenceInputStream(entity.getContent(), mimeType, encoding);
    }

    // /**
    // * Starting point for the SAAJ - SOAP Client Testing
    // */
    // public static void main(String args[]) {
    // try {
    // // Create SOAP Connection
    // SOAPConnectionFactory soapConnectionFactory =
    // SOAPConnectionFactory.newInstance();
    // SOAPConnection soapConnection = soapConnectionFactory.createConnection();
    //
    // // Send SOAP Message to SOAP Server
    // String url = "https://polar.geodacenter.org:8443/wfs-soap";
    // SOAPMessage soapResponse = soapConnection.call(createSOAPRequest(), url);
    //
    // // Process the SOAP Response
    // printSOAPResponse(soapResponse);
    //
    // soapConnection.close();
    // } catch (Exception e) {
    // System.err.println("Error occurred while sending SOAP Request to
    // Server");
    // e.printStackTrace();
    // }
    // }
    //
    // private static SOAPMessage createSOAPRequest() throws Exception {
    //
    // MessageFactory messageFactory = MessageFactory.newInstance();
    // SOAPMessage soapMessage = messageFactory.createMessage();
    //
    // SOAPPart soapPart = soapMessage.getSOAPPart();
    //
    // XmlObject soapEnvelope =
    // XmlObject.Factory.parse(SOAPTest.class.getResourceAsStream("GetFeatureSOAP.xml"));
    //
    // soapPart.adoptNode(soapEnvelope.getDomNode());
    //
    ////
    //// String serverURI = "http://ws.cdyne.com/";
    ////
    //// // SOAP Envelope
    //// SOAPEnvelope envelope = soapPart.getEnvelope();
    //// envelope.addNamespaceDeclaration("example", serverURI);
    ////
    //// /*
    //// Constructed SOAP Request Message:
    //// <SOAP-ENV:Envelope
    // xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
    // xmlns:example="http://ws.cdyne.com/">
    //// <SOAP-ENV:Header/>
    //// <SOAP-ENV:Body>
    //// <example:VerifyEmail>
    //// <example:email>mutantninja@gmail.com</example:email>
    //// <example:LicenseKey>123</example:LicenseKey>
    //// </example:VerifyEmail>
    //// </SOAP-ENV:Body>
    //// </SOAP-ENV:Envelope>
    //// */
    ////
    //// // SOAP Body
    //// SOAPBody soapBody = envelope.getBody();
    //// SOAPElement soapBodyElem = soapBody.addChildElement("VerifyEmail",
    // "example");
    //// SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("email",
    // "example");
    //// soapBodyElem1.addTextNode("mutantninja@gmail.com");
    //// SOAPElement soapBodyElem2 = soapBodyElem.addChildElement("LicenseKey",
    // "example");
    //// soapBodyElem2.addTextNode("123");
    ////
    //// MimeHeaders headers = soapMessage.getMimeHeaders();
    //// headers.addHeader("SOAPAction", serverURI + "VerifyEmail");
    ////
    //// soapMessage.saveChanges();
    ////
    //// /* Print the request message */
    //// System.out.print("Request SOAP Message = ");
    //// soapMessage.writeTo(System.out);
    //// System.out.println();
    //
    // return soapMessage;
    // }
    //
    // /**
    // * Method used to print the SOAP Response
    // */
    // private static void printSOAPResponse(SOAPMessage soapResponse) throws
    // Exception {
    // TransformerFactory transformerFactory = TransformerFactory.newInstance();
    // Transformer transformer = transformerFactory.newTransformer();
    // Source sourceContent = soapResponse.getSOAPPart().getContent();
    // System.out.print("\nResponse SOAP Message = ");
    // StreamResult result = new StreamResult(System.out);
    // transformer.transform(sourceContent, result);
    // }

}