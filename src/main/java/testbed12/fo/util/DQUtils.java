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
package testbed12.fo.util;


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
 * This Utility-Class ... TODO
 *
 * @author Maurin Radtke (m.radtke@52north.org)
 *
 */
public class DQUtils {

    private DQ_AbsoluteExternalPositionalAccuracy dqaepa;
    Document doc;

    public DQUtils(DQ_AbsoluteExternalPositionalAccuracy dq_aepa) {
        this.dqaepa = dq_aepa;
    }

    public DQUtils(XmlObject wpsResultXML) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(wpsResultXML.toString()));

            Document document = (Document) builder.parse(is);

            XPath xpath = XPathFactory.newInstance().newXPath();

            String characterString = "//Result/Output/Data/DQ_AbsoluteExternalPositionalAccuracy/result/DQ_ConformanceResult/explanation/CharacterString/text()";
            String passBoolean = "//Result/Output/Data/DQ_AbsoluteExternalPositionalAccuracy/result/DQ_ConformanceResult/pass/Boolean/text()";

            Node cSNode = (Node) xpath.evaluate(characterString, document, XPathConstants.NODE);
            Node pBNode = (Node) xpath.evaluate(passBoolean, document, XPathConstants.NODE);

            String resultCharacterString = cSNode.getNodeValue();
            String resultpassBoolean = pBNode.getNodeValue();

            Double displacementMean = Double.parseDouble(
                    resultCharacterString.substring(
                            resultCharacterString.lastIndexOf(" ")
                    )
            );
            boolean pass = (Integer.parseInt(resultpassBoolean) != 0);

            this.dqaepa = new DQ_AbsoluteExternalPositionalAccuracy(displacementMean, pass);

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public double getMeanDisplacement() {
        return this.dqaepa.getDisplacementMean();
    }

    public boolean getPassBoolean() {
        return this.dqaepa.getPass();
    }
;

}
