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
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.n52.wps.server.request.strategy.ReferenceInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Algorithm(version="1.0.0", abstrakt="Process acting as async facade for a SOAP endpoint" )
public class AsyncFacadeProcess extends AbstractAnnotatedAlgorithm {

    private static final Logger log = LoggerFactory.getLogger(AsyncFacadeProcess.class);

    private XmlObject soapRequest;
    private URI endpointURL;

    private XmlObject wfsResponse;

    @Execute
    public void echo() {
        log.debug("Requesting WFS SOAP: {}", endpointURL);
        
        String mimeType = "application/soap+xml";
        
        try {

            InputStream inputStream = httpPost(endpointURL.toString(), soapRequest.toString(), mimeType);
            
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
            
            wfsResponse = XmlObject.Factory.parse(soapString);
            
        } catch (XmlException e) {
            log.error(e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        log.debug("Finished requesting SOAP service.");
    }

    @ComplexDataOutput(identifier = "soap-response", binding = GenericXMLDataBinding.class)
    public XmlObject getComplexOutput() {
        return wfsResponse;
    }

    @ComplexDataInput(binding = GenericXMLDataBinding.class, identifier = "soap-request", minOccurs = 1, maxOccurs = 1)
    public void setComplexInput(XmlObject soapRequest) {
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
        
        asyncFacadeProcess.setLiteralInput(new URI("http://polar.geodacenter.org/services/ows/wfs/soap/1.2/mtom/sec"));
        
        String soapRequest = "<soap:Envelope"+
                "    xmlns:soap='http://www.w3.org/2003/05/soap-envelope' >"+
                "    <soap:Header>"+
                "        <wsse:Security"+
                "            xmlns:wsse='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd'"+
                "            xmlns:wsu='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd'  soap:mustUnderstand='1'>"+
                "            <wsse:UsernameToken wsu:Id='UsernameToken-a612a4ab-667a-4774-bc49-8c6c5833ebc1'>"+
                "                <wsse:Username>guest</wsse:Username>"+
                "                <wsse:Password Type='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText'>123456</wsse:Password>"+
                "            </wsse:UsernameToken>"+
                "        </wsse:Security>"+
                "    </soap:Header>"+
                "    <soap:Body>"+
                "        <GetCapabilities service='WFS'"+
                "            xmlns='http://www.opengis.net/wfs/2.0'"+
                "            xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='http://www.opengis.net/wfs/2.0 http://schemas.opengis.net/wfs/2.0/wfs.xsd'/>"+
                "        </soap:Body>"+
                "    </soap:Envelope>";
        
        asyncFacadeProcess.setComplexInput(XmlObject.Factory.parse(soapRequest));
        
        asyncFacadeProcess.echo();
        
    }
}
