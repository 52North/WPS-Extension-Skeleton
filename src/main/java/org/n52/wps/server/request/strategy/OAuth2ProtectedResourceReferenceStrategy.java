package org.n52.wps.server.request.strategy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.xmlbeans.XmlObject;
import org.n52.geoprocessing.oauth2.AccessTokenResponse;
import org.n52.geoprocessing.oauth2.OAuth2Client;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.request.InputReference;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.exceptions.UnirestException;

import net.opengis.ows.x20.DomainType;
import net.opengis.ows.x20.OperationDocument.Operation;
import net.opengis.ows.x20.ValuesReferenceDocument.ValuesReference;
import net.opengis.wfs.x20.WFSCapabilitiesDocument;
import net.opengis.wps.x20.CapabilitiesDocument;

public class OAuth2ProtectedResourceReferenceStrategy implements IReferenceStrategy {
    
    private static Logger LOGGER = LoggerFactory.getLogger(OAuth2ProtectedResourceReferenceStrategy.class);
    
    private String bearerTokenReference = "urn:ogc:def:security:authentication:ietf:6750:Bearer";
        
    @Override
    public boolean isApplicable(InputReference input) {
        
        //try to fetch resource
        XmlObject payload = null;
        
        if(input.isSetBody()){
            payload = input.getBody();
        }
        
        String href = input.getHref();
        
        boolean protectedResource = false;
        
        try {
            protectedResource = checkIfProtectedResource(href, (payload != null ? payload.xmlText() : null));
        } catch (IOException e) {
            LOGGER.error("Could not fetch from URL: "  + href);
            if(payload != null){
                LOGGER.trace("Payload: " + payload.xmlText());
            }
        }
        
        if(!protectedResource){
            return false;//strategy not applicable for unprotected resources
        }
        
        //if 401 not authorized is returned (and if the service is an OWS), try to fetch capabilities
        //if oauth2 protected, return true        
        try {
            return checkIfCapabilitiesContainOAuth2Constraint(href);
        } catch (Exception e) {
            LOGGER.error("Could not check capabilities.", e);
        }
        
        return false;
    }

    @Override
    public ReferenceInputStream fetchData(InputReference input) throws ExceptionReport {

        OAuth2ReferenceParsingCM configModule =  (OAuth2ReferenceParsingCM) WPSConfig.getInstance().getConfigurationModuleForClass(this.getClass().getName(), ConfigurationCategory.GENERAL);
        
        String clientID = configModule.getClientId();
        String clientSecret = configModule.getClientSecret();
        String audience = configModule.getAudience();
        String tokenEndpointString = configModule.getTokenEndpoint();
        URL tokenEndpoint;
        
        try {
            tokenEndpoint = new URL(tokenEndpointString);
        } catch (MalformedURLException e) {
            throw new ExceptionReport("Could not create URL from token_endpoint parameter: " + tokenEndpointString, ExceptionReport.NO_APPLICABLE_CODE);
        }
        
        //get new access token with client credentials        
        String accessToken = "";
        
        try {
            AccessTokenResponse accessTokenResponse = new OAuth2Client().getAccessToken(tokenEndpoint, clientID, clientSecret, audience);
            if(accessTokenResponse.isError()){
                throw new ExceptionReport("Could not get access token URL from token_endpoint. Error: " + accessTokenResponse.getErrorCause(), ExceptionReport.NO_APPLICABLE_CODE);//TODO adjust exception text
            }
            accessToken = accessTokenResponse.getAccessToken();
        } catch (UnirestException | IOException e) {
            throw new ExceptionReport("Could not get access token URL from token_endpoint: " + tokenEndpointString, ExceptionReport.NO_APPLICABLE_CODE);//TODO adjust exception text
        }
        
        if(accessToken == null || accessToken.isEmpty()){
            throw new ExceptionReport("Could not get access token URL from token_endpoint: " + tokenEndpointString, ExceptionReport.NO_APPLICABLE_CODE);//TODO adjust exception text
        }
        
        //let the Thread sleep a bit, as the access token might not be valid already
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            // TODO: handle exception
        }
        
        //send token to service in Authorization header
        String href = input.getHref();
        String mimeType = input.getMimeType();

        try {
            if (input.isSetBody()) {
                String body = input.getBody().toString();
                return httpPost(href, body, mimeType, accessToken);
            }

            // Handle get request
            else {
                return httpGet(href, mimeType, accessToken);
            }

        }
        catch(RuntimeException e) {
            throw new ExceptionReport("Error occured while parsing XML",
                                        ExceptionReport.NO_APPLICABLE_CODE, e);
        }
        catch(MalformedURLException e) {
            String inputID = input.getIdentifier();
            throw new ExceptionReport("The inputURL of the execute is wrong: inputID: " + inputID + " | dataURL: " + href,
                                        ExceptionReport.INVALID_PARAMETER_VALUE );
        }
        catch(IOException e) {
             String inputID = input.getIdentifier();
             throw new ExceptionReport("Error occured while receiving the complexReferenceURL: inputID: " + inputID + " | dataURL: " + href,
                                     ExceptionReport.INVALID_PARAMETER_VALUE );
        }
    }

    /**
     * Make a GET request using mimeType and href
     *
     * TODO: add support for autoretry, proxy
     */
    private ReferenceInputStream httpGet(final String dataURLString, final String mimeType, String accessToken) throws IOException {
        HttpClient backend = new DefaultHttpClient();
        DecompressingHttpClient httpclient = new DecompressingHttpClient(backend);

        HttpGet httpget = new HttpGet(dataURLString);

        if (mimeType != null){
            httpget.addHeader(new BasicHeader("Content-type", mimeType));
        }

        httpget.addHeader(new BasicHeader("Authorization", accessToken));

        return processResponse(httpclient.execute(httpget));
    }

    /**
     * Make a POST request using mimeType and href
     *
     * TODO: add support for autoretry, proxy
     */
    private ReferenceInputStream httpPost(final String dataURLString, final String body, final String mimeType, String accessToken) throws IOException {
        HttpClient backend = new DefaultHttpClient();

        DecompressingHttpClient httpclient = new DecompressingHttpClient(backend);

        HttpPost httppost = new HttpPost(dataURLString);

        if (mimeType != null){
            httppost.addHeader(new BasicHeader("Content-type", mimeType));
        }

        httppost.addHeader(new BasicHeader("Authorization", accessToken));
        
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
    
    private boolean checkIfCapabilitiesContainOAuth2Constraint(String originalRequest) throws Exception{
                
        ServiceType serviceType = null;
        
        if(originalRequest.toLowerCase().contains("wps")){
            
            serviceType = ServiceType.WPS;
            
        }else if(originalRequest.toLowerCase().contains("wfs")){
            
            serviceType = ServiceType.WFS;
        }
        
        OperationType operationType = getOperationType(originalRequest);
        
        try {
            InputStream capabilitiesInputStream = requestCapabilities(originalRequest, serviceType);

            switch (serviceType) {
            case WPS:            
                CapabilitiesDocument wpsCapsDoc = CapabilitiesDocument.Factory.parse(capabilitiesInputStream);

                return checkIfOperationIsProtected(wpsCapsDoc.getCapabilities().getOperationsMetadata().getOperationArray(), operationType);
                
            case WFS:
                WFSCapabilitiesDocument wfsCapsDoc = WFSCapabilitiesDocument.Factory.parse(capabilitiesInputStream);
                                
                return checkIfOperationIsProtected(wfsCapsDoc.getWFSCapabilities().getOperationsMetadata().getOperationArray(), operationType);

            default:
                break;
            } 
            
        } catch (IOException e) {
            LOGGER.error("Could not request capabilities for original request:" + originalRequest);
            return false;
        }
        
        return false;
    }
    
    private boolean checkIfOperationIsProtected(net.opengis.ows.x11.OperationDocument.Operation[] operationArray,
            OperationType operationType) {
        
        for (net.opengis.ows.x11.OperationDocument.Operation operation : operationArray) {
            
            if(operation.getName().toLowerCase().equals(operationType.toString().toLowerCase())){
                
                net.opengis.ows.x11.DomainType[] constraintArray = operation.getConstraintArray();
                
                for (net.opengis.ows.x11.DomainType domainType : constraintArray) {
                    if(domainType.isSetValuesReference()){
                        net.opengis.ows.x11.ValuesReferenceDocument.ValuesReference valuesReference = domainType.getValuesReference();
                        
                        String referenceValue = valuesReference.getStringValue();
                        
                        if(referenceValue != null && referenceValue.equals(bearerTokenReference)){
                            return true;
                        }
                        
                    }
                }
                
            }
            
        }
        return false;
    }

    private boolean checkIfOperationIsProtected(Operation[] operationArray, OperationType operationType){
        
        for (Operation operation : operationArray) {
            
            if(operation.getName().toLowerCase().equals(operationType.toString().toLowerCase())){
                
                DomainType[] constraintArray = operation.getConstraintArray();
                
                for (DomainType domainType : constraintArray) {
                    if(domainType.isSetValuesReference()){
                        
                        ValuesReference valuesReference = domainType.getValuesReference();
                        
                        String referenceValue = valuesReference.getStringValue();
                        
                        if(referenceValue != null && referenceValue.equals(bearerTokenReference)){
                            return true;
                        }
                        
                    }
                }
                
            }
            
        }
        
        return false;
        
    }
    
    private OperationType getOperationType(String originalRequest) {
        
        originalRequest = originalRequest.toLowerCase();
        
        if(originalRequest.contains(OperationType.EXECUTE.toString().toLowerCase())){
            return OperationType.EXECUTE;
        }else if(originalRequest.contains(OperationType.DESCRIBEPROCESS.toString().toLowerCase())){
            return OperationType.DESCRIBEPROCESS;
        }else if(originalRequest.contains(OperationType.INSERTPROCESS.toString().toLowerCase())){
            return OperationType.INSERTPROCESS;
        }else if(originalRequest.contains(OperationType.DESCRIBEFEATURETYPE.toString().toLowerCase())){
            return OperationType.DESCRIBEFEATURETYPE;
        }else if(originalRequest.contains(OperationType.GETFEATURE.toString().toLowerCase())){
            return OperationType.GETFEATURE;
        }
        
        return OperationType.UNDEFINED;
    }

    private String createGetCapabilitiesRequest(String originalRequest, ServiceType serviceType){
        
        String capabilitiesURL = "";
        
        //get index of question mark as separator between base url and request
        int indexOfQuestionmark = originalRequest.indexOf("?");
        
        if(indexOfQuestionmark < 0){
            LOGGER.info("Request doesn't seem to be OWS request. It doesn't contain a question mark: " + originalRequest);
            return capabilitiesURL;
        }
        
        capabilitiesURL = originalRequest.substring(0, indexOfQuestionmark + 1);
        
        capabilitiesURL = capabilitiesURL.concat("request=GetCapabilities");
        
        //we have to differentiate bewteen wps and wfs, because of the different version handling
        //we only will request version 2.0.0 of each service
        switch (serviceType) {
        case WPS:            
            capabilitiesURL = capabilitiesURL.concat("&service=WPS&acceptVersions=2.0.0");
            
            break;
        case WFS:
            capabilitiesURL = capabilitiesURL.concat("&service=WFS&version=2.0.0");
            
            break;

        default:
            break;
        }
        
        return capabilitiesURL;
    }
    
    private InputStream requestCapabilities(String originalRequest, ServiceType serviceType) throws IOException{
        
        String getCapabilitiesURL = createGetCapabilitiesRequest(originalRequest, serviceType);
        
        if(getCapabilitiesURL == null || getCapabilitiesURL.isEmpty()){
            return null;
        }
        
        // Send data
        URL url = new URL(getCapabilitiesURL);

        URLConnection conn = url.openConnection();

        conn.setDoOutput(true);
        
        return conn.getInputStream();
    }
    
    private boolean checkIfProtectedResource(String targetURL,
            String payload) throws IOException {
        // Send data
        URL url = new URL(targetURL);

        URLConnection conn = url.openConnection();

        conn.setDoOutput(true);

        if (payload != null) {

            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

            wr.write(payload);
            wr.close();

        }

        try {
            conn.getInputStream();
            // resource can be accessed, so ot protected
            return false;
        } catch (IOException e) {
            LOGGER.info("Exception while trying to fetch resource.");
        }

        int statusCode = ((HttpURLConnection) conn).getResponseCode();

        LOGGER.info("Status code: " + statusCode);

        if (statusCode == 401) {
            return true;
        }
        return false;
    }

    enum ServiceType{
        
        WPS, WFS
        
    }
    
    enum OperationType{
        
        DESCRIBEPROCESS, EXECUTE, GETFEATURE, DESCRIBEFEATURETYPE, INSERTPROCESS, UNDEFINED
    }
    
}
