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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.ExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotatedExtensionAlgorithmTest {

	private static final Logger logger = LoggerFactory.getLogger(AnnotatedExtensionAlgorithmTest.class);
	
	@Test
	public void testAlgo() throws ExceptionReport {
		Map<String, List<IData>> map = readData();
		
		AnnotatedExtensionAlgorithm algo = new AnnotatedExtensionAlgorithm();
		Map<String, IData> result = algo.run(map);
		
		Assert.assertTrue("Result not available!", result.get(resolveOutputIdentifier(algo)) != null);
		logger.info("Succesfully retrieved result: '{}'", result.get(resolveOutputIdentifier(algo)));
	}

	private String resolveOutputIdentifier(AnnotatedExtensionAlgorithm algo) {
		String id = algo.getDescription().getProcessOutputs().getOutputArray(0)
				.getIdentifier().getStringValue();
		logger.info("Output identifier is '{}'", id);
		return id;
	}

	private Map<String, List<IData>> readData() {

		LiteralStringBinding stringBinding = new LiteralStringBinding("input");
		                
		List<IData> inputDataList1 = new ArrayList<IData>();
		inputDataList1.add(stringBinding);
		
		Map<String,List<IData>> map = new HashMap<String, List<IData>>();
		map.put(ExtensionAlgorithm.INPUT, inputDataList1);
		return map;
	}
	
}
