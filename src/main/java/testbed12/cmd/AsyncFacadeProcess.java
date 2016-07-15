package testbed12.cmd;

import java.io.IOException;
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

            ReferenceInputStream inputStream = httpPost(endpointURL.toString(), soapRequest.toString(), mimeType);
            
            StringBuilder builder = new StringBuilder();
            
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
    
    
}
