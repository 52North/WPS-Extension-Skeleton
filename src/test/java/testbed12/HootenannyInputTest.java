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
package testbed12;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import testbed13.dsi.util.DQUtils;
import testbed13.dsi.util.DQ_AbsoluteExternalPositionalAccuracy;

/**
 *
 * @author Maurin Radtke (m.radtke@52north.org)
 */
public class HootenannyInputTest {

    Logger LOGGER = LoggerFactory.getLogger(HootenannyInputTest.class);
    String projectRoot = "";

    DQUtils utils;

    @Before
    public void setUp() {
        File f = new File(this.getClass().getProtectionDomain().getCodeSource()
                .getLocation().getFile());
        projectRoot = f.getParentFile().getParentFile().getParent();
    }

    @Test
    public void testParsing() {
        String testFilePath = projectRoot
                + "\\WPS-Extension-Skeleton\\src\\test\\resources\\testbed13\\2017-06-14 Data Quality Response.xml";
        XmlObject obj = null;
        try {
            testFilePath = URLDecoder.decode(testFilePath, "UTF-8");
            File testFile = new File(testFilePath);
            obj = XmlObject.Factory.parse(testFile);
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        } catch (XmlException xmle) {
            fail(xmle.getMessage());
        }
        ;

        utils = new DQUtils(obj);

        try {
            assertEquals(28.552085167979794, utils.getMeanDisplacement(), 0.00000000001);
            assertTrue(utils.getPassBoolean() == false);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testParsing2() {
        String testFilePath = projectRoot
                + "\\WPS-Extension-Skeleton\\src\\test\\resources\\testbed13\\2017-06-14 Data Quality Response2.txt";

        XmlObject obj = null;
        try {
            testFilePath = URLDecoder.decode(testFilePath, "UTF-8");
            File testFile = new File(testFilePath);
            obj = XmlObject.Factory.parse(testFile);
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        } catch (XmlException xmle) {
            fail(xmle.getMessage());
        }
        ;

        utils = new DQUtils(obj);
        try {
            assertEquals(9.485969554370216, utils.getMeanDisplacement(), 0.00000000001);
            assertTrue(utils.getPassBoolean() == true);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }

    @Test
    public void testParsing3() {
        String testFilePath = projectRoot
                + "\\WPS-Extension-Skeleton\\src\\test\\resources\\testbed13\\2017-06-14 Data Quality Object.xml";

        XmlObject obj = null;
        try {
            testFilePath = URLDecoder.decode(testFilePath, "UTF-8");
            File testFile = new File(testFilePath);
            obj = XmlObject.Factory.parse(testFile);
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        } catch (XmlException xmle) {
            fail(xmle.getMessage());
        }
        ;

        utils = new DQUtils(new DQ_AbsoluteExternalPositionalAccuracy(obj));
        try {
            assertEquals(28.552085167979794, utils.getMeanDisplacement(), 0.00000000001);
            assertTrue(utils.getPassBoolean() == false);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }
    }
}
