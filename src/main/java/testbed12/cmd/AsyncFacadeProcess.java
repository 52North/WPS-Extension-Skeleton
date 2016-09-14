package testbed12.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

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
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.io.data.binding.complex.GenericXMLDataBinding;
import org.n52.wps.io.data.binding.complex.PlainStringBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.n52.wps.server.request.strategy.ReferenceInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Algorithm(version="1.0.0", abstrakt="Process acting as async facade for a SOAP endpoint" )
public class AsyncFacadeProcess extends AbstractAnnotatedAlgorithm {

    private static final Logger log = LoggerFactory.getLogger(AsyncFacadeProcess.class);

    private String soapRequest;
    private URI endpointURL;

    private String wfsResponse;

    @Execute
    public void echo() {
        log.debug("Requesting WFS SOAP: {}", endpointURL);
        
        String mimeType = "application/soap+xml";
        
        log.debug("Requesting: " + endpointURL.toString());
        
        try {

            InputStream inputStream = httpPost(endpointURL.toString(), soapRequest, mimeType);
            
            StringBuilder builder = new StringBuilder();
            
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//            
//            String line = "";
//            
//            boolean startToAppend = false;
//            
//            while((line = bufferedReader.readLine()) != null){
//                
//                if(startToAppend || line.toLowerCase().contains("envelope")){
//                    if(!startToAppend){
//                        startToAppend = true;
//                    }
//                    builder.append(line);
//                }                
//            }
            
            int i = 0;
            
            while((i = inputStream.read()) != -1){
                builder.append((char)i);
            }
            String soapString = builder.toString();
            
            wfsResponse = soapString.replace("&", "&amp;");
            
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        log.debug("Finished requesting SOAP service.");
    }

    @ComplexDataOutput(identifier = "soap-response", binding = PlainStringBinding.class)
    public String getComplexOutput() {
        return wfsResponse;
    }

    @ComplexDataInput(binding = PlainStringBinding.class, identifier = "soap-request", minOccurs = 1, maxOccurs = 1)
    public void setComplexInput(String soapRequest) {
        this.soapRequest = soapRequest;
    }

    @LiteralDataInput(identifier = "endpoint-url", minOccurs = 1, maxOccurs = 1)
    public void setLiteralInput(URI endpointURL) {
        this.endpointURL = endpointURL;
    }
    
    /**
     * Make a POST request using mimeType and href
     * 
     * TODO: add support for autoretry, proxy
     */
    private InputStream httpPost(final String dataURLString,
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

    private InputStream processResponse(HttpResponse response) throws IOException {

        HttpEntity entity = response.getEntity();
        Header header;

        header = entity.getContentType();
        String mimeType = header == null ? null : header.getValue();

        header = entity.getContentEncoding();
        String encoding = header == null ? null : header.getValue();
        
//        Header[] contentTypes = response.getHeaders("Content-Type");
//        
//        try {
//            
//            HeaderElement firstContentTypeHeaderFirstElement = response.getFirstHeader("Content-Type").getElements()[0];
//            
//            String headerName = firstContentTypeHeaderFirstElement.getName();
//            
//            if(headerName.equals("multipart/related")){
//                String boundary = firstContentTypeHeaderFirstElement.getParameterByName("boundary").getValue();
//                
//                PipedInputStream inputStream = new PipedInputStream();
////                
////                PipedOutputStream outputStream = new PipedOutputStream(inputStream); 
//                MultipartStream multipartStream = new MultipartStream(entity.getContent(), boundary.getBytes());
////                multipartStream.readBoundary();
//                multipartStream.readBodyData(System.err);
//                
////                outputStream.flush();
//                
//                return inputStream;
//            }
//            
//        } catch (Exception e) {
//            log.error("Could not get header information from response. " + response.toString());
//            log.error(e.getMessage());
//        }
        
        return new ReferenceInputStream(entity.getContent(), mimeType, encoding);
    }
    
    public static void main(String[] args) throws Exception {
        AsyncFacadeProcess asyncFacadeProcess = new AsyncFacadeProcess();
        
//        asyncFacadeProcess.setLiteralInput(new URI("http://polar.geodacenter.org/services/ows/wfs/soap/1.2/mtom/sec"));
//        
//        String soapRequest = "<soap:Envelope"+
//                "    xmlns:soap='http://www.w3.org/2003/05/soap-envelope' >"+
//                "    <soap:Header>"+
//                "        <wsse:Security"+
//                "            xmlns:wsse='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd'"+
//                "            xmlns:wsu='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd'  soap:mustUnderstand='1'>"+
//                "            <wsse:UsernameToken wsu:Id='UsernameToken-a612a4ab-667a-4774-bc49-8c6c5833ebc1'>"+
//                "                <wsse:Username>guest</wsse:Username>"+
//                "                <wsse:Password Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText'>123456</wsse:Password>"+
//                "            </wsse:UsernameToken>"+
//                "        </wsse:Security>"+
//                "    </soap:Header>"+
//                "    <soap:Body>"+
//                "        <GetCapabilities service='WFS'"+
//                "            xmlns='http://www.opengis.net/wfs/2.0'"+
//                "            xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.opengis.net/wfs/2.0 http://schemas.opengis.net/wfs/2.0/wfs.xsd'/>"+
//                "        </soap:Body>"+
//                "    </soap:Envelope>";
        
        asyncFacadeProcess.setLiteralInput(new URI("http://tb12.secure-dimensions.com/soap/services"));
        
        String soapRequest = "<soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Header xmlns:wsa=\"http://www.w3.org/2005/08/addressing\"><wsa:To>https://tb12.secure-dimensions.com/soap/services/ows_proxy</wsa:To><wsa:Action>http://www.opengis.net/wfs/requests#GetCapabilities</wsa:Action><wsa:MessageID>urn:uuid:ae6c2f16-c6a1-1e51-226b-002522163135</wsa:MessageID><wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\" soapenv:mustUnderstand=\"1\"><wsse:BinarySecurityToken EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"CertID-ae6d196c-c6a1-1e51-226c\" ValueType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3\">MIIDDDCCAfSgAwIBAgIQM6YEf7FVYx/tZyEXgVComTANBgkqhkiG9w0BAQUFADAwMQ4wDAYDVQQKDAVPQVNJUzEeMBwGA1UEAwwVT0FTSVMgSW50ZXJvcCBUZXN0IENBMB4XDTA1MDMxOTAwMDAwMFoXDTE4MDMxOTIzNTk1OVowQjEOMAwGA1UECgwFT0FTSVMxIDAeBgNVBAsMF09BU0lTIEludGVyb3AgVGVzdCBDZXJ0MQ4wDAYDVQQDDAVBbGljZTCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEAoqi99By1VYo0aHrkKCNT4DkIgPL/SgahbeKdGhrbu3K2XG7arfD9tqIBIKMfrX4Gp90NJa85AV1yiNsEyvq+mUnMpNcKnLXLOjkTmMCqDYbbkehJlXPnaWLzve+mW0pJdPxtf3rbD4PS/cBQIvtpjmrDAU8VsZKT8DN5Kyz+EZsCAwEAAaOBkzCBkDAJBgNVHRMEAjAAMDMGA1UdHwQsMCowKKImhiRodHRwOi8vaW50ZXJvcC5iYnRlc3QubmV0L2NybC9jYS5jcmwwDgYDVR0PAQH/BAQDAgSwMB0GA1UdDgQWBBQK4l0TUHZ1QV3V2QtlLNDm+PoxiDAfBgNVHSMEGDAWgBTAnSj8wes1oR3WqqqgHBpNwkkPDzANBgkqhkiG9w0BAQUFAAOCAQEABTqpOpvW+6yrLXyUlP2xJbEkohXHI5OWwKWleOb9hlkhWntUalfcFOJAgUyH30TTpHldzx1+vK2LPzhoUFKYHE1IyQvokBN2JjFO64BQukCKnZhldLRPxGhfkTdxQgdf5rCK/wh3xVsZCNTfuMNmlAM6lOAg8QduDah3WFZpEA0s2nwQaCNQTNMjJC8tav1CBr6+E5FAmwPXP7pJxn9Fw9OXRyqbRA4v2y7YpbGkG2GI9UvOHw6SGvf4FRSthMMO35YbpikGsLix3vAsXWWi4rwfVOYzQK0OFPNi9RMCUdSH06m9uLWckiCxjos0FQODZE9l4ATGy9s9hNVwryOJTw==</wsse:BinarySecurityToken><wsse:UsernameToken xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" wsu:Id=\"SigID-ae6d8e9c-c6a1-1e51-226d\"><wsse:Username>Alice</wsse:Username><wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest\">+43faXkM59Wvc/1EqkhqFYGJwOs=</wsse:Password><wsse:Nonce>p3aGzO+yN4I8oTehOJTY3ZrIcYsM6XJ8</wsse:Nonce><wsu:Created>2016-01-29T16:02:22.307Z</wsu:Created></wsse:UsernameToken><ds:Signature xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" Id=\"SigID-ae6db5ac-c6a1-1e51-226e\"><ds:SignedInfo><ds:CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"></ds:CanonicalizationMethod><ds:SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"></ds:SignatureMethod><ds:Reference URI=\"#SigID-ae6d8e9c-c6a1-1e51-226d\"><ds:Transforms><ds:Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"></ds:Transform></ds:Transforms><ds:DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"></ds:DigestMethod><ds:DigestValue>sk06OjMEwRt9U0PZYPTR3CVycL4=</ds:DigestValue></ds:Reference></ds:SignedInfo><ds:SignatureValue>bcI24Ds8MvM7dvgAC3WeO+uv9zKAq6yB3qdI02t8I1h7W8GVBrPYs4f7a9JdELtV0UsA1NUOp4nbPwtPUdw+nVURHQUHr+mZ879HZuzD3daW5Z3kTk9i1+6ceCbZh/K/SRCFOQvDpKRC74W5iTkik7TBALgPl7sJxVt7nY6GBhs=</ds:SignatureValue><ds:KeyInfo><wsse:SecurityTokenReference><wsse:Reference URI=\"#CertID-ae6d196c-c6a1-1e51-226c\" ValueType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3\"></wsse:Reference></wsse:SecurityTokenReference></ds:KeyInfo></ds:Signature></wsse:Security></soapenv:Header>"+
                "<soapenv:Body>"+
                "<GetCapabilities"+
                  "service=\"WFS\""+
                  "version=\"2.0.0\""+
                  "xmlns=\"http://www.opengis.net/wfs/2.0\""+
                  "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+
                  "xsi:schemaLocation=\"http://www.opengis.net/wfs/2.0"+
                  "http://schemas.opengis.net/wfs/2.0.0/wfs.xsd\"/>"+
              "</soapenv:Body>"+
            "</soapenv:Envelope>";
        
        asyncFacadeProcess.setComplexInput(soapRequest);
        
        asyncFacadeProcess.echo();
        
    }
}
