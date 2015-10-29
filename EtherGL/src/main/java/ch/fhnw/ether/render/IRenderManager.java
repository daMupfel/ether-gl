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

package ch.fhnw.ether.render;

import ch.fhnw.ether.scene.camera.ICamera;
import ch.fhnw.ether.scene.camera.IViewCameraState;
import ch.fhnw.ether.scene.light.ILight;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.view.IView;
import ch.fhnw.util.math.Mat4;

/**
 * Render manager interface for interaction between scene and renderer.
 *
 * @author radar
 */
public interface IRenderManager {
	void addView(IView view);

	void removeView(IView view);

	/**
	 * Add mesh to renderer.
	 * 
	 * @param mesh
	 *            mesh to be added
	 * @throws IllegalArgumentException
	 *             if mesh already in renderer.
	 */
	void addMesh(IMesh mesh);

	/**
	 * Remove mesh from renderer.
	 * 
	 * @param mesh
	 *            mesh to be removed
	 * @throws IllegalArgumentException
	 *             if mesh not in renderer.
	 */
	void removeMesh(IMesh mesh);

	/**
	 * Add light to renderer..
	 * 
	 * @param light
	 *            light to be added
	 * @throws IllegalArgumentException
	 *             if light already in renderer.
	 */
	void addLight(ILight light);

	/**
	 * Remove light from renderer.
	 * 
	 * @param light
	 *            light to be removed
	 * @throws IllegalArgumentException
	 *             if light not in renderer.
	 */
	void removeLight(ILight light);

	/**
	 * Get active camera for given view.
	 */
	ICamera getCamera(IView view);
	
	/**
	 * Set active camera for given view.
	 */
	void setCamera(IView view, ICamera camera);

	/**
	 * Lock camera for given view with provided matrices.
	 */
	void lockCamera(IView view, Mat4 viewMatrix, Mat4 projMatrix);

	/**
	 * Get view-camera state.
	 */
	IViewCameraState getViewCameraState(IView view);
	
	/**
	 * Create render state for next frame and return it as a runnable to be run
	 * on render thread.
	 * 
	 * @return runnable to be run on render thread
	 */
	Runnable getRenderRunnable();
}
