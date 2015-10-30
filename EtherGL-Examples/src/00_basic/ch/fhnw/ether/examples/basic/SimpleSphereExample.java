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

import java.awt.event.KeyEvent;

import ch.fhnw.ether.controller.DefaultController;
import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.scene.DefaultScene;
import ch.fhnw.ether.scene.IScene;
import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.IMesh.Queue;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive;
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial;
import ch.fhnw.ether.scene.mesh.material.ColorMaterial;
import ch.fhnw.ether.scene.mesh.material.PointMaterial;
import ch.fhnw.ether.scene.mesh.material.Texture;
import ch.fhnw.ether.ui.Button;
import ch.fhnw.ether.view.IView;
import ch.fhnw.ether.view.gl.DefaultView;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Transform;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.GeodesicSphere;

public final class SimpleSphereExample {
	public static void main(String[] args) {
		new SimpleSphereExample();
	}

	public SimpleSphereExample() {
		// Create controller
		IController controller = new DefaultController();
		controller.run((time) -> {
			// Create view
			new DefaultView(controller, 100, 100, 500, 500, IView.INTERACTIVE_VIEW, "Simple Sphere");
	
			// Create scene and add some content
			IScene scene = new DefaultScene(controller);
			controller.setScene(scene);
	
			GeodesicSphere sphere = new GeodesicSphere(3);
	
			IMesh transparentMeshT = new DefaultMesh(new ColorMaterial(new RGBA(1, 1, 1, 0.5f)), DefaultGeometry.createV(Primitive.TRIANGLES, sphere.getTriangles()), Queue.TRANSPARENCY);
			IMesh transparentMeshL = new DefaultMesh(new ColorMaterial(new RGBA(1, 1, 1, 1)), DefaultGeometry.createV(Primitive.LINES, sphere.getLines()), Queue.TRANSPARENCY);
			IMesh transparentMeshP = new DefaultMesh(new PointMaterial(new RGBA(1, 1, 0, 0.5f), 8), DefaultGeometry.createV(Primitive.POINTS, sphere.getPoints()), Queue.TRANSPARENCY);
	
			transparentMeshT.setPosition(Vec3.X_NEG);
			transparentMeshL.setPosition(Vec3.X_NEG);
			transparentMeshP.setPosition(Vec3.X_NEG);
	
			transparentMeshT.setTransform(Transform.trs(0, 0, 0, 0, 0, 0, 0.1f, 0.1f, 0.1f));
			transparentMeshL.setTransform(Transform.trs(0, 0, 0, 0, 0, 0, 0.1f, 0.1f, 0.1f));
			transparentMeshP.setTransform(Transform.trs(0, 0, 0, 0, 0, 0, 0.1f, 0.1f, 0.1f));
	
			IMesh solidMeshT = new DefaultMesh(new ColorMaterial(new RGBA(0.5f, 0.5f, 0.5f, 1)), DefaultGeometry.createV(Primitive.TRIANGLES, sphere.getTriangles()), Queue.DEPTH);
			IMesh solidMeshL = new DefaultMesh(new ColorMaterial(new RGBA(1, 1, 1, 1)), DefaultGeometry.createV(Primitive.LINES, sphere.getLines()), Queue.DEPTH);
			IMesh solidMeshP = new DefaultMesh(new PointMaterial(new RGBA(1, 1, 0, 1), 8), DefaultGeometry.createV(Primitive.POINTS, sphere.getPoints()), Queue.DEPTH);
	
			solidMeshT.setPosition(Vec3.X);
			solidMeshL.setPosition(Vec3.X);
			solidMeshP.setPosition(Vec3.X);
	
			solidMeshT.setTransform(Transform.trs(0, 0, 0, 0, 0, 0, 0.1f, 0.1f, 0.1f));
			solidMeshL.setTransform(Transform.trs(0, 0, 0, 0, 0, 0, 0.1f, 0.1f, 0.1f));
			solidMeshP.setTransform(Transform.trs(0, 0, 0, 0, 0, 0, 0.1f, 0.1f, 0.1f));
	
			Texture t = new Texture(SimpleSphereExample.class.getResource("assets/earth_nasa.jpg"));
			IMesh texturedMeshT = new DefaultMesh(new ColorMapMaterial(t), DefaultGeometry.createVM(Primitive.TRIANGLES, sphere.getTriangles(), sphere.getTexCoords()), Queue.DEPTH);
			texturedMeshT.setPosition(Vec3.ZERO);
			
			scene.add3DObjects(transparentMeshT, transparentMeshL, transparentMeshP, solidMeshT, solidMeshL, solidMeshP, texturedMeshT);
	
			// Add an exit button
			controller.getUI().addWidget(new Button(0, 0, "Quit", "Quit", KeyEvent.VK_ESCAPE, (button, v) -> System.exit(0)));
		});
	}
}
