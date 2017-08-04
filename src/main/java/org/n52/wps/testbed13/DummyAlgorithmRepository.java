package org.n52.wps.testbed13;

import java.util.Collection;
import java.util.Collections;

import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.ProcessDescription;

public class DummyAlgorithmRepository implements IAlgorithmRepository {

	@Override
	public Collection<String> getAlgorithmNames() {
		return Collections.emptyList();
	}

	@Override
	public IAlgorithm getAlgorithm(String processID) {
		return null;
	}

	@Override
	public ProcessDescription getProcessDescription(String processID) {
		return null;
	}

	@Override
	public boolean containsAlgorithm(String processID) {
		return false;
	}

	@Override
	public void shutdown() {		
	}

}
