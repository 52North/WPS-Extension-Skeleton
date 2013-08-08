package org.n52.wps.server.algorithm.jts;
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
 * This program is free software; you can redistribute and/or modify it under 
 * the terms of the GNU General Public License version 2 as published by the 
 * Free Software Foundation.
 * 
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 * 
 */
import org.apache.log4j.Logger;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.io.data.binding.complex.JTSGeometryBinding;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

/**
 * This algorithm creates a buffers around a JTS geometry using the build-in buffer-method.
 * @author Benjamin Pross (bpross-52n)
 *
 */
@Algorithm(version = "1.0.0", abstrakt="This algorithm creates a buffers around a JTS geometry using the build-in buffer-method.")
public class JTSBufferAlgorithm extends AbstractAnnotatedAlgorithm {

    private static Logger LOGGER = Logger.getLogger(JTSBufferAlgorithm.class);

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