package org.n52.wps.server.request.strategy;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.request.InputReference;
import org.n52.wps.webapp.api.ConfigurationCategory;
import org.n52.wps.webapp.api.ConfigurationManager;
import org.n52.wps.webapp.api.ConfigurationManagerImpl;
import org.n52.wps.webapp.api.ConfigurationModule;
import org.n52.wps.webapp.service.ConfigurationService;

import net.opengis.wps.x100.InputType;

public class OAuthReferenceStrategyTest {

    @Mock
    private OAuth2ReferenceParsingCM configModule;
    
    @Mock
    private WPSConfig wpsConfig;

    @InjectMocks
    private ConfigurationManager configurationManager;
    
    @Mock
    private ConfigurationService configurationService;
    
    @Before
    public void setup() {
        configurationManager = new ConfigurationManagerImpl();
        MockitoAnnotations.initMocks(this);
        WPSConfig.getInstance().setConfigurationManager(configurationManager);
    }

    @After
    public void tearDown() {
        configModule = null;
    }
    
    @Test
    public void testOAuth2ReferenceStrategy() {

        InputType inputType = InputType.Factory.newInstance();

        inputType.addNewReference().setHref(
                "http://localhost:8080/SecurityProxy/service/wfs?service=WFS&version=2.0.0&request=GetFeature&typeName=tb13:tnm-manhattan-streets&count=50&outputFormat=gml3");

        InputReference inputReference = new InputReference(inputType);

        boolean isProtected = new OAuth2ProtectedResourceReferenceStrategy().isApplicable(inputReference);

        System.out.println("IsApplicable: " + isProtected);
        
    }

    @Test
    public void testFetchData(){
        
        when(configModule.getClientId()).thenReturn("vnL3I6Tk1JQgSOn47SjwfTjycwv5J5Lq");
        when(configModule.getClientSecret()).thenReturn("MrFYHqprwUQCchLoBK9HOGgXqLcZRq45l8JuJJ9XTm_Vs3oiNSnFdRfbRDfi2x0x");
        when(configModule.getAudience()).thenReturn("http://tb12.dev.52north.org/geoserver/tb13/ows");
        when(configModule.getTokenEndpoint()).thenReturn("https://bpross-52n.eu.auth0.com/oauth/token");
        when(configModule.getClassName()).thenReturn(OAuth2ProtectedResourceReferenceStrategy.class.getName());
        
        Map<String, ConfigurationModule> configModules = new HashMap<>();
        
        configModules.put(OAuth2ReferenceParsingCM.class.getName(), configModule);
        
        when(configurationService.getActiveConfigurationModulesByCategory(ConfigurationCategory.GENERAL)).thenReturn(configModules);
//        when(WPSConfig.getInstance().getConfigurationModuleForClass(OAuth2ProtectedResourceReferenceStrategy.class.getName(), ConfigurationCategory.GENERAL)).thenReturn(configModule);
        
        InputType inputType = InputType.Factory.newInstance();

        inputType.addNewReference().setHref(
                "http://localhost:8080/SecurityProxy/service/wfs?service=WFS&version=2.0.0&request=GetFeature&typeName=tb13:tnm-manhattan-streets&count=50&outputFormat=gml3");

        InputReference inputReference = new InputReference(inputType);
        
        try {
            ReferenceInputStream inputStream = new OAuth2ProtectedResourceReferenceStrategy().fetchData(inputReference);
            
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            
            String line = "";
            
            while((line = bufferedReader.readLine()) != null){
                System.out.println(line);
            }
            
            bufferedReader.close();
            
        } catch (ExceptionReport | IOException e) {
            fail(e.getMessage());
        }
    }
    
}
