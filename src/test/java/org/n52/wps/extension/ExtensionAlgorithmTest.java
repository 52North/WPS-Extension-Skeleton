package org.n52.wps.extension;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.parser.GML3BasicParser;
import org.n52.wps.server.ExceptionReport;

public class ExtensionAlgorithmTest {

	@Test
	public void testAlgo() throws ExceptionReport {
		Map<String, List<IData>> map = readData();
		
		ExtensionAlgorithm algo = new ExtensionAlgorithm();
		Map<String, IData> result = algo.run(map);
		
		Assert.assertTrue("Result not available!", result.get(ExtensionAlgorithm.OUTPUT) != null);
	}

	private Map<String, List<IData>> readData() {
		InputStream inputStream = getClass().getResourceAsStream("Layer1.gml");
		                
		GML3BasicParser parser = new GML3BasicParser();
		IData layer1 = parser.parse(inputStream, "text/xml; subtype=gml/3.0.0", null);
		                
		List<IData> inputDataList1 = new ArrayList<IData>();
		inputDataList1.add(layer1);
		
		Map<String,List<IData>> map = new HashMap<String, List<IData>>();
		map.put(ExtensionAlgorithm.INPUT, inputDataList1);
		return map;
	}
	
}
