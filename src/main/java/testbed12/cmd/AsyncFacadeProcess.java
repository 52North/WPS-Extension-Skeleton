package testbed12.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.commons.context.ExecutionContextFactory;
import org.n52.wps.io.data.binding.complex.PlainStringBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.n52.wps.server.request.strategy.ReferenceInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.opengis.wps.x100.OutputDefinitionType;

@Algorithm(version="1.0.0", abstrakt="Process acting as async facade for a SOAP endpoint" )
public class AsyncFacadeProcess extends AbstractAnnotatedAlgorithm {

    private static final Logger log = LoggerFactory.getLogger(AsyncFacadeProcess.class);

    private final String inputRequest = "request";
    private final String inputEndpointURL = "endpoint-url";
    private final String outputResponse = "response";
    
    private String request;
    private URI endpointURL;
    private String response;

    @Execute
    public void echo() {
        log.debug("Requesting service: {}", endpointURL);
        
        List<OutputDefinitionType> outputDefinitions = ExecutionContextFactory.getContext().getOutputs();

        String mimeType = "text/xml";
        
		if (outputDefinitions.size() != 0) {

			for (OutputDefinitionType outputDefinitionType : outputDefinitions) {
				if (outputDefinitionType.getIdentifier().getStringValue().equals(inputRequest)) {
					mimeType = outputDefinitionType.getMimeType();
				}
			}
		}else{
			if(request.contains("soap")){
				mimeType = "application/soap+xml";
			}
		}
        
        log.debug("Using mime type: " + mimeType);
        
        try {

            InputStream inputStream = httpPost(endpointURL.toString(), request, mimeType);
            
            StringBuilder builder = new StringBuilder();
            
            int i = 0;
            
            while((i = inputStream.read()) != -1){
                builder.append((char)i);
            }
            String resonseString = builder.toString();
            
            response = resonseString.replace("&", "&amp;");
            
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        log.debug("Finished requesting service.");
    }

    @ComplexDataOutput(identifier = outputResponse, binding = PlainStringBinding.class)
    public String getComplexOutput() {
        return response;
    }

    @ComplexDataInput(binding = PlainStringBinding.class, identifier = inputRequest, minOccurs = 1, maxOccurs = 1)
    public void setComplexInput(String soapRequest) {
        this.request = soapRequest;
    }

    @LiteralDataInput(identifier = inputEndpointURL, minOccurs = 1, maxOccurs = 1)
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
        
        asyncFacadeProcess.setLiteralInput(new URI("https://polar.geodacenter.org:8443/geoserver29/testbed12/ows"));
        
        String request = "<wfs:GetFeature service=\"WFS\" version=\"1.1.0\""+
        		"  xmlns:topp=\"http://www.openplans.org/topp\""+
        		"  xmlns:wfs=\"http://www.opengis.net/wfs\""+
        		"  xmlns:ogc=\"http://www.opengis.net/ogc\""+
        		"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""+
        		"  xsi:schemaLocation=\"http://www.opengis.net/wfs"+
        		"                      http://schemas.opengis.net/wfs/1.1.0/wfs.xsd\">"+
        		"  <wfs:Query typeName=\"us-roads\"/>"+
        		"</wfs:GetFeature>";
        
        asyncFacadeProcess.setComplexInput(request);
        
        asyncFacadeProcess.echo();
        
    }
}
