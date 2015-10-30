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
import ch.fhnw.ether.render.shader.base.AbstractShader;
import ch.fhnw.ether.render.variable.base.FloatUniform;
import ch.fhnw.ether.render.variable.builtin.ColorArray;
import ch.fhnw.ether.render.variable.builtin.PositionArray;
import ch.fhnw.ether.render.variable.builtin.ViewUniformBlock;
import ch.fhnw.ether.scene.DefaultScene;
import ch.fhnw.ether.scene.IScene;
import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive;
import ch.fhnw.ether.scene.mesh.material.CustomMaterial;
import ch.fhnw.ether.view.IView;
import ch.fhnw.ether.view.gl.DefaultView;

public final class CustomShaderExample {
	public static class ExampleCustomMaterial extends CustomMaterial {
		private volatile float redGain;
		
		public ExampleCustomMaterial(ExampleCustomShader shader, float redGain) {
			super(shader);
			this.redGain = redGain;
		}
		
		public float getRedGain() {
			return redGain;
		}
		
		public void setRedGain(float redGain) {
			this.redGain = redGain;
		}
		
		@Override
		public void getAttributes(IAttributes attributes) {
			attributes.provide(new MaterialAttribute<Float>("custom.red_gain"), () -> redGain);
			super.getAttributes(attributes);
		}
	}
	
	public static class ExampleCustomShader extends AbstractShader {
		public ExampleCustomShader() {
			super(CustomShaderExample.class, "custom_shader_example.custom_shader", "custom_shader", Primitive.TRIANGLES);
			addArray(new PositionArray());
			addArray(new ColorArray());

			addUniform(new ViewUniformBlock());
			addUniform(new FloatUniform("custom.red_gain", "redGain"));
		}
	}

	public static void main(String[] args) {
		new CustomShaderExample();
	}

	private static IMesh makeColoredTriangle() {
		float[] vertices = { 0, 0, 0, 0, 0, 0.5f, 0.5f, 0, 0.5f };
		float[] colors = { 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 1 };

		DefaultGeometry g = DefaultGeometry.createVC(Primitive.TRIANGLES, vertices, colors);
		return new DefaultMesh(new ExampleCustomMaterial(new ExampleCustomShader(), 2f), g);
	}
	
	private volatile IMesh mesh;

	// Setup the whole thing
	public CustomShaderExample() {
		// Create controller
		IController controller = new DefaultController();
		controller.run((time) -> {
			// Create view
			new DefaultView(controller, 100, 100, 500, 500, IView.INTERACTIVE_VIEW, "Test");

			// Create scene and add triangle
			IScene scene = new DefaultScene(controller);
			controller.setScene(scene);

			mesh = makeColoredTriangle();
			scene.add3DObject(mesh);
		});
		controller.animate((time, interval) -> {
			((ExampleCustomMaterial)mesh.getMaterial()).setRedGain((float)Math.sin(time) + 1);
			mesh.updateRequest();
			return true;
		});
	}
}
