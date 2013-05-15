package org.n52.wps.extension;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
import org.n52.wps.server.ExceptionReport;

/**
 * Simple mockup algorithm doing nothing special.
 * 
 * @author matthes rieke
 *
 */
public class ExtensionAlgorithm extends AbstractSelfDescribingAlgorithm {

	static final String INPUT = "inputLayer";
	static final String OUTPUT = "resultOutput";

	public Class<?> getInputDataType(String identifier) {
		if (identifier.equals(INPUT)) {
			return GTVectorDataBinding.class;
		}
		return null;
	}

	public Class<?> getOutputDataType(String identifier) {
		if (identifier.equals(OUTPUT)) {
			return GTVectorDataBinding.class;
		}
		return null;
	}

	public Map<String, IData> run(Map<String, List<IData>> data)
			throws ExceptionReport {
		GTVectorDataBinding binding = new GTVectorDataBinding(null);
		Map<String,IData> result = new HashMap<String, IData>();
		result.put(OUTPUT, binding);
		return result;
	}

	@Override
	public List<String> getInputIdentifiers() {
		return Collections.singletonList(INPUT);
	}

	@Override
	public List<String> getOutputIdentifiers() {
		return Collections.singletonList(OUTPUT);
	}

}
