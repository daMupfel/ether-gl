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

import java.util.List;

import ch.fhnw.ether.controller.DefaultController;
import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.controller.event.IKeyEvent;
import ch.fhnw.ether.formats.obj.OBJReader;
import ch.fhnw.ether.scene.DefaultScene;
import ch.fhnw.ether.scene.IScene;
import ch.fhnw.ether.scene.light.DirectionalLight;
import ch.fhnw.ether.scene.light.ILight;
import ch.fhnw.ether.scene.light.PointLight;
import ch.fhnw.ether.scene.light.SpotLight;
import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.IMesh.Flags;
import ch.fhnw.ether.scene.mesh.IMesh.Queue;
import ch.fhnw.ether.scene.mesh.MeshLibrary;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive;
import ch.fhnw.ether.scene.mesh.material.ColorMaterial;
import ch.fhnw.ether.scene.mesh.material.IMaterial;
import ch.fhnw.ether.scene.mesh.material.ShadedMaterial;
import ch.fhnw.ether.scene.mesh.material.Texture;
import ch.fhnw.ether.ui.Button;
import ch.fhnw.ether.view.IView;
import ch.fhnw.ether.view.gl.DefaultView;
import ch.fhnw.util.color.RGB;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Transform;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.GeodesicSphere;

public final class SimpleLightExample {
	private static final String[] HELP = { 
		//@formatter:off
		"Simple Light Example", 
		"", 
		"[1] Directional Light [2] Point Light [3] Spot Light",
		"Use cursors to move light position / direction x/y axis",
		"Use q/a to move light position / direction along z axis",
		"", 
		"Use Mouse Buttons + Shift or Mouse Wheel to Navigate" 
		//@formatter:on
	};

	public static void main(String[] args) {
		new SimpleLightExample();
	}

	private static final boolean ADD_BUNNY = true;

	private static final float INC_XY = 0.25f;
	private static final float INC_Z = 0.25f;
	private static final RGB AMBIENT = RGB.BLACK;
	private static final RGB COLOR = RGB.WHITE;

	private IController controller;
	private IScene scene;
	private ILight light = new DirectionalLight(Vec3.Z, AMBIENT, COLOR);
	private IMesh lightMesh;

	public SimpleLightExample() {
		// Create controller
		controller = new DefaultController() {
			@Override
			public void keyPressed(IKeyEvent e) {
				switch (e.getKeyCode()) {
				case IKeyEvent.VK_1:
					scene.remove3DObject(light);
					light = new DirectionalLight(light.getPosition(), AMBIENT, COLOR);
					scene.add3DObject(light);
					break;
				case IKeyEvent.VK_2:
					scene.remove3DObject(light);
					light = new PointLight(light.getPosition(), AMBIENT, COLOR, 10);
					scene.add3DObject(light);
					break;
				case IKeyEvent.VK_3:
					scene.remove3DObject(light);
					light = new SpotLight(light.getPosition(), AMBIENT, COLOR, 10, Vec3.Z_NEG, 15, 0);
					scene.add3DObject(light);
					break;
				case IKeyEvent.VK_UP:
					lightMesh.setPosition(lightMesh.getPosition().add(Vec3.Y.scale(INC_XY)));
					break;
				case IKeyEvent.VK_DOWN:
					lightMesh.setPosition(lightMesh.getPosition().add(Vec3.Y_NEG.scale(INC_XY)));
					break;
				case IKeyEvent.VK_LEFT:
					lightMesh.setPosition(lightMesh.getPosition().add(Vec3.X_NEG.scale(INC_XY)));
					break;
				case IKeyEvent.VK_RIGHT:
					lightMesh.setPosition(lightMesh.getPosition().add(Vec3.X.scale(INC_XY)));
					break;
				case IKeyEvent.VK_Q:
					lightMesh.setPosition(lightMesh.getPosition().add(Vec3.Z.scale(INC_Z)));
					break;
				case IKeyEvent.VK_A:
					lightMesh.setPosition(lightMesh.getPosition().add(Vec3.Z_NEG.scale(INC_Z)));
					break;
				case IKeyEvent.VK_H:
					printHelp(HELP);
					break;
				default:
					super.keyPressed(e);
				}
				getUI().setMessage("light position: " + lightMesh.getPosition());
				light.setPosition(lightMesh.getPosition());
				lightMesh.updateRequest(null);
			};
		};

		controller.run((time) -> {
			// Create view
			new DefaultView(controller, 100, 100, 500, 500, IView.INTERACTIVE_VIEW, "Simple Sphere");
	
			// Create scene and add some content
			scene = new DefaultScene(controller);
			controller.setScene(scene);
	
			// Add first light and light geometry
			GeodesicSphere s = new GeodesicSphere(4);
	
			lightMesh = new DefaultMesh(new ColorMaterial(RGBA.YELLOW), DefaultGeometry.createV(Primitive.TRIANGLES, s.getTriangles()), Flags.DONT_CAST_SHADOW);
			lightMesh.setTransform(Transform.trs(0, 0, 0, 0, 0, 0, 0.1f, 0.1f, 0.1f));
			lightMesh.setPosition(new Vec3(0, 0, 2));
			light.setPosition(lightMesh.getPosition());
	
			scene.add3DObjects(light);
			scene.add3DObjects(lightMesh);
	
			// Add a second light (now that we have multiple light support...)
			scene.add3DObject(new PointLight(new Vec3(2, 0, 2), RGB.BLACK, RGB.BLUE));
	
			// Add a ground plane
			IMesh ground = MeshLibrary.createGroundPlane();
			scene.add3DObject(ground);
	
			// Add an exit button
			controller.getUI().addWidget(new Button(0, 0, "Quit", "Quit", IKeyEvent.VK_ESCAPE, (button, v) -> System.exit(0)));
	
			// Add geometry
			IMaterial solidMaterial = new ShadedMaterial(RGB.BLACK, RGB.BLUE, RGB.GRAY, RGB.WHITE, 10, 1, 1f);
			IMaterial lineMaterial = new ColorMaterial(new RGBA(1, 1, 1, 0.2f));
	
			Texture t = new Texture(SimpleLightExample.class.getResource("assets/earth_nasa.jpg"));
			IMaterial textureMaterial = new ShadedMaterial(RGB.BLACK, RGB.BLUE, RGB.GRAY, RGB.RED, 10, 1, 1f, t);
	
			IMesh solidMeshT = new DefaultMesh(solidMaterial, DefaultGeometry.createVN(Primitive.TRIANGLES, s.getTriangles(), s.getNormals()));
			IMesh solidMeshL = new DefaultMesh(lineMaterial, DefaultGeometry.createV(Primitive.LINES, s.getLines()), Queue.TRANSPARENCY);
	
			solidMeshT.setTransform(Transform.trs(-1, 0, 0.5f, 0, 0, 0, 1, 1, 1));
			solidMeshL.setTransform(Transform.trs(-1, 0, 0.5f, 0, 0, 0, 1, 1, 1));
	
			IMesh texturedMeshT = new DefaultMesh(textureMaterial, DefaultGeometry.createVNM(Primitive.TRIANGLES, s.getTriangles(), s.getNormals(),
					s.getTexCoords()));
			texturedMeshT.setTransform(Transform.trs(1, 0, 0.5f, 0, 0, 0, 1, 1, 1));
	
			IMesh solidCubeT = MeshLibrary.createCube(solidMaterial);
			solidCubeT.setTransform(Transform.trs(0, 0, 0.5f, 0, 0, 0, 0.8f, 0.8f, 0.8f));
	
			scene.add3DObjects(solidMeshT, solidMeshL, texturedMeshT, solidCubeT);
	
			// Add bunny
			IMesh solidBunnyT = null;
			if (ADD_BUNNY) {
				try {
					List<IMesh> meshes = new OBJReader(getClass().getResource("assets/bunny_original.obj")).getMeshes();
					solidBunnyT = new DefaultMesh(solidMaterial, meshes.get(0).getGeometry());
					solidBunnyT.setTransform(Transform.trs(2, 0, 0, 90, 0, 0, 4, 4, 4));
					scene.add3DObject(solidBunnyT);
				} catch (Throwable throwable) {
					throwable.printStackTrace();
				}
			}
		});
	}
}
