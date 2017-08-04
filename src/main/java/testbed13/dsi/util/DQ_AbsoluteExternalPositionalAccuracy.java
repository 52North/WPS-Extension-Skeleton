/*
 * Copyright 2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package testbed13.dsi.util;

import java.io.File;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.xmlbeans.XmlObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * This DQ_AbsoluteExternalPositionalAccuracy ... TODO
 *
 * @author Maurin Radtke (m.radtke@52north.org)
 *
 */
public class DQ_AbsoluteExternalPositionalAccuracy {
    
    private double displacementMean;
    private boolean pass;
    
    public DQ_AbsoluteExternalPositionalAccuracy(XmlObject payloadXML){
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(payloadXML.toString()));
            
            Document document = (Document) builder.parse(is);

            XPath xpath = XPathFactory.newInstance().newXPath();
            
            String characterString = "//DQ_AbsoluteExternalPositionalAccuracy/result/DQ_ConformanceResult/explanation/CharacterString/text()";
            String passBoolean = "//DQ_AbsoluteExternalPositionalAccuracy/result/DQ_ConformanceResult/pass/Boolean/text()";
            
            Node cSNode = (Node) xpath.evaluate(characterString, document, XPathConstants.NODE);
            Node pBNode = (Node) xpath.evaluate(passBoolean, document, XPathConstants.NODE);
            
            String resultCharacterString = cSNode.getNodeValue();
            String resultpassBoolean = pBNode.getNodeValue();
            
            this.displacementMean = Double.parseDouble(
                    resultCharacterString.substring(
                            resultCharacterString.lastIndexOf(" ")
                    )
            );
            this.pass = (Integer.parseInt(resultpassBoolean) != 0);
              
        } catch (Exception e) {
            System.out.println(e.toString());
            throw new RuntimeException(e.toString());
        }
    }

    public DQ_AbsoluteExternalPositionalAccuracy(double displacementMean, boolean pass){
        this.displacementMean = displacementMean;
        this.pass = pass;
    }

    public boolean getPass(){
        return this.pass;
    }
    
    public double getDisplacementMean(){
        return this.displacementMean;
    }
    
}