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

package ch.fhnw.ether.examples.basic;

import ch.fhnw.ether.controller.DefaultController;
import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.controller.event.IScheduler;
import ch.fhnw.ether.scene.DefaultScene;
import ch.fhnw.ether.scene.IScene;
import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.IGeometryAttribute;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive;
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial;
import ch.fhnw.ether.scene.mesh.material.IMaterial;
import ch.fhnw.ether.scene.mesh.material.Texture;
import ch.fhnw.ether.view.IView;
import ch.fhnw.ether.view.gl.DefaultView;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Mat4;

public final class SimpleTextureExample {

	public static void main(String[] args) {
		new SimpleTextureExample();
	}

	private static IMesh makeTexturedTriangle() {
		float[] vertices = { 0, 0, 0, 0, 0, 0.5f, 0.5f, 0, 0.5f };
		float[] colors = { 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 1 };
		float[] texCoords = { 0, 0, 0, 1, 1, 1 };

		
		try {
			IMaterial m = new ColorMapMaterial(RGBA.WHITE, new Texture(SimpleTextureExample.class.getResource("assets/fhnw_logo.jpg")), true);
			IGeometry g = DefaultGeometry.createVCM(Primitive.TRIANGLES, vertices, colors, texCoords);
			return new DefaultMesh(m, g);
		} catch (Exception e) {
			System.err.println("cant load image");
			System.exit(1);
		}
		return null;
	}

	private IMesh mesh;

	// Setup the whole thing
	public SimpleTextureExample() {
		// Create controller
		IController controller = new DefaultController();
		controller.run((time) -> {
			// Create view
			new DefaultView(controller, 100, 100, 500, 500, IView.INTERACTIVE_VIEW, "Test");
	
			// Create scene and add triangle
			IScene scene = new DefaultScene(controller);
			controller.setScene(scene);
	
			mesh = makeTexturedTriangle();
			scene.add3DObject(mesh);
		});

		// Animate (Using event timer)
		controller.animate(new IScheduler.IAnimationAction() {
			private int c = 0;

			@Override
			public boolean run(double time, double interval) {

				// make some heavy animation calculation
				c += 4;
				if (c >= 360)
					c = 0;
				float f = 0.4f + 0.6f * (float) (Math.sin(Math.toRadians(c)) * 0.5 + 1);

				// apply changes to geometry
				mesh.setTransform(Mat4.scale(f, f, f));
				mesh.getGeometry().modify(1, (IGeometryAttribute id, float[] vertices) -> {
					for (int i = 0; i < vertices.length; ++i) {
						if (i % 4 == 3)
							continue;
						vertices[i] -= 0.2f * (1 - f);
						if (vertices[i + 0] <= 0)
							vertices[i + 0] = 1;
					}
				});
				mesh.updateRequest();
				return true;
			}
		});
	}
}
