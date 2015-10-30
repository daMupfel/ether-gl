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

package ch.fhnw.ether.examples.video.fx;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.SwingUtilities;

import ch.fhnw.ether.controller.DefaultController;
import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.image.RGB8Frame;
import ch.fhnw.ether.media.RenderProgram;
import ch.fhnw.ether.scene.DefaultScene;
import ch.fhnw.ether.scene.IScene;
import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.IMesh.Queue;
import ch.fhnw.ether.scene.mesh.MeshLibrary;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.Primitive;
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial;
import ch.fhnw.ether.scene.mesh.material.Texture;
import ch.fhnw.ether.video.AbstractVideoSource;
import ch.fhnw.ether.video.CameraInfo;
import ch.fhnw.ether.video.CameraSource;
import ch.fhnw.ether.video.URLVideoSource;
import ch.fhnw.ether.view.IView.Config;
import ch.fhnw.ether.view.IView.ViewType;
import ch.fhnw.ether.view.gl.DefaultView;
import ch.fhnw.util.math.Transform;

public class SimplePlayerGL implements Runnable {
	private static final float  SCALE  = 3.5f;

	private final AbstractVideoSource<?> source;
	private final Texture                texture = new Texture(new RGB8Frame(16, 16));

	public SimplePlayerGL(AbstractVideoSource<?> source) {
		this.source = source;
	}

	@Override
	public void run() {
		try {
			IController controller = new DefaultController();

			new DefaultView(controller, 0, 10, 1024, 512, new Config(ViewType.INTERACTIVE_VIEW, 2), "SimplePlayerGL");

			IScene scene = new DefaultScene(controller);
			controller.setScene(scene);

			DefaultGeometry g = DefaultGeometry.createVM(Primitive.TRIANGLES, MeshLibrary.DEFAULT_QUAD_TRIANGLES, MeshLibrary.DEFAULT_QUAD_TEX_COORDS); 
			IMesh mesh = new DefaultMesh(new ColorMapMaterial(texture), g, Queue.TRANSPARENCY);
			mesh.setTransform(Transform.trs(0, 0, 0, 90, 0, 0, SCALE * source.getWidth() / source.getHeight(), SCALE, SCALE));			
			scene.add3DObject(mesh);

			texture.useProgram(new RenderProgram<>(source));
			texture.start();
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		AbstractVideoSource<?> source;
		if(args.length == 0)
			source =  CameraSource.create(CameraInfo.getInfos()[0]);
		else {
			try {
				source = new URLVideoSource(new URL(args[0]));
			} catch(MalformedURLException e) {
				source = new URLVideoSource(new File(args[0]).toURI().toURL());
			}
		}
		SwingUtilities.invokeLater(new SimplePlayerGL(source));
	}
}
