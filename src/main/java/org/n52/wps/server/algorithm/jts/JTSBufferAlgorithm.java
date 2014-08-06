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
package org.n52.wps.server.algorithm.jts;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.io.data.binding.complex.JTSGeometryBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

/**
 * This algorithm creates a buffers around a JTS geometry using the build-in buffer-method.
 * @author Benjamin Pross (bpross-52n)
 *
 */
@Algorithm(version = "1.0.0", abstrakt="This algorithm creates a buffers around a JTS geometry using the build-in buffer-method.")
public class JTSBufferAlgorithm extends AbstractAnnotatedAlgorithm {

    private static Logger LOGGER = LoggerFactory.getLogger(JTSBufferAlgorithm.class);

    public JTSBufferAlgorithm() {
        super();
    }
    
    private Geometry result;
	private Geometry data;
	private double distance;
	private int quadrantSegments;
	private int endCapStyle;

    @ComplexDataOutput(identifier = "result", binding = JTSGeometryBinding.class)
    public Geometry getResult() {
        return result;
    }

    @ComplexDataInput(identifier = "data", binding = JTSGeometryBinding.class, minOccurs=1)
    public void setData(Geometry data) {
        this.data = data;
    }
    
    @LiteralDataInput(identifier="distance", minOccurs=1)
    public void setDistance(double distance) {
		this.distance = distance;
	}
    
    @LiteralDataInput(identifier="quadrantSegments", defaultValue="" + BufferParameters.DEFAULT_QUADRANT_SEGMENTS, minOccurs=0)
	public void setQuadrantSegments(int quadrantSegments) {
		this.quadrantSegments = quadrantSegments;
	}
    
    @LiteralDataInput(identifier="endCapStyle", abstrakt="CAP_ROUND = 1, CAP_FLAT = 2, CAP_SQUARE = 3", allowedValues={"1","2","3"}, defaultValue = ""+ BufferParameters.CAP_ROUND, minOccurs=0)
	public void setEndCapStyle(int endCapStyle) {
		this.endCapStyle = endCapStyle;
	}

    @Execute
    public void runAlgorithm() {
    	LOGGER.info("Buffering");
    	result = data.buffer(distance, quadrantSegments, endCapStyle);
    }
}