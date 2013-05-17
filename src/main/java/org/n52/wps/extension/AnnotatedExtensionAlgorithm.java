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

import java.lang.annotation.Annotation;

import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

/**
 * Simple mockup algorithm doing nothing special.
 * This time in an {@link Annotation} version.
 * 
 * @author matthes rieke
 *
 */
@Algorithm(
		version = "0.1",
		abstrakt = "Simple mockup algorithm doing nothing special",
		title = "Simple Algoritm",
		//identifier = "your-identifer",
		statusSupported = false,
		storeSupported = false)
public class AnnotatedExtensionAlgorithm extends AbstractAnnotatedAlgorithm {

	private String output;
	
	@LiteralDataInput(
			identifier = "input",
			title = "useless input",
			abstrakt = "Whatever you put in, it won't change anything.")
	public String input;
	
	@LiteralDataOutput(identifier = "output",
			title = "sophisticated output",
			abstrakt = "what will you expect as the output?")
	public String getOutput() {
		return this.output;
	}
	
	@Execute
	public void myRunMethodFollowingNoSyntaxNoArgumentsAllowed() {
		this.output = "works like a charm.";
	}

}
