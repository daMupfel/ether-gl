/*
 * Copyright (c) 2013 - 2015 Stefan Muller Arisona, Simon Schubiger, Samuel von Stachelski
 * Copyright (c) 2013 - 2015 FHNW & ETH Zurich
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *  Neither the name of FHNW / ETH Zurich nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ch.fhnw.ether.examples.metrobuzz;

import java.io.IOException;

import ch.fhnw.ether.examples.metrobuzz.controller.MetroBuzzController;
import ch.fhnw.ether.examples.metrobuzz.controller.View;
import ch.fhnw.ether.examples.metrobuzz.io.matsim.Loader;
import ch.fhnw.ether.examples.metrobuzz.model.Scene;
import ch.fhnw.ether.view.IView;

public class MetroBuzz {
	public static void main(final String[] args) {
		new MetroBuzz(args);
	}

	public MetroBuzz(String[] args) {
		if (args.length < 1)
			throw new IllegalArgumentException("Pass path to Sioux OSM as command line argument");

		MetroBuzzController controller = new MetroBuzzController();

		new View(controller, 0, 10, 512, 512, IView.INTERACTIVE_VIEW);
		new View(controller, 512, 10, 512, 512, IView.MAPPED_VIEW);

		Scene scene = new Scene(controller);
		System.out.println("Loading Data");
		try {
			Loader.load(scene, args[0], 100 /* Integer.MAX_VALUE */);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Done.");

		Scene.printAgent(scene.getAgents().get(0));
		// Model.printAgent(model.getAgents().get(1));
		// Model.printAgent(model.getAgents().get(2));

		controller.setScene(scene);
	}

}
