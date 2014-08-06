/**
 * Copyright (C) 2013
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.wps.extension;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.parser.GML2BasicParser;
import org.n52.wps.io.datahandler.parser.GML3BasicParser;
import org.n52.wps.server.ExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtensionAlgorithmTest {
	
	private static final Logger logger = LoggerFactory.getLogger(ExtensionAlgorithmTest.class);

	@Test
	public void testAlgo() throws ExceptionReport {
		Map<String, List<IData>> map = readData();
		
		ExtensionAlgorithm algo = new ExtensionAlgorithm();
		Map<String, IData> result = algo.run(map);
		
		Assert.assertTrue("Result not available!", result.get(ExtensionAlgorithm.OUTPUT) != null);
		logger.info("Succesfully retrieved result: '{}'", result.get(ExtensionAlgorithm.OUTPUT));
	}

	private Map<String, List<IData>> readData() {
		InputStream inputStream = getClass().getResourceAsStream("Layer1.gml");
		                
		GML2BasicParser parser = new GML2BasicParser();
		IData layer1 = parser.parse(inputStream, "text/xml; subtype=gml/2.1.2", null);
		                
		List<IData> inputDataList1 = new ArrayList<IData>();
		inputDataList1.add(layer1);
		
		Map<String,List<IData>> map = new HashMap<String, List<IData>>();
		map.put(ExtensionAlgorithm.INPUT, inputDataList1);
		return map;
	}
	
}
