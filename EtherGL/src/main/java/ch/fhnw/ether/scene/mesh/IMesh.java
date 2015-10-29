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

package ch.fhnw.ether.scene.mesh;

import java.util.EnumSet;

import ch.fhnw.ether.scene.I3DObject;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry;
import ch.fhnw.ether.scene.mesh.material.IMaterial;
import ch.fhnw.util.IUpdateListener;
import ch.fhnw.util.math.Mat4;

/**
 * Basic mesh abstraction. A mesh is a light weight structure that combines
 * render pass, scene/view/render flags, material, geometry, and a transform.
 * 
 * 
 * @author radar
 *
 */
public interface IMesh extends I3DObject, IUpdateListener {

	enum Queue {
		DEPTH, TRANSPARENCY, OVERLAY, DEVICE_SPACE_OVERLAY, SCREEN_SPACE_OVERLAY
	}

	// FIXME: DONT_CAST_SHADOW should go to material, including CULL_FACE / DONT_CULL_FACE
	enum Flags {
		DONT_CAST_SHADOW, INTERACTIVE_VIEWS_ONLY
	}

	EnumSet<Flags> NO_FLAGS = EnumSet.noneOf(Flags.class);

	Queue getQueue();

	EnumSet<Flags> getFlags();

	IMaterial getMaterial();

	IGeometry getGeometry();

	Mat4 getTransform();

	void setTransform(Mat4 transform);

	

	// XXX below methods are to be used from the renderer thread only. subject to change / cleanup

	/**
	 * @return true if material was modified since last call to this method.
	 */
	boolean needsMaterialUpdate();

	/**
	 * @return true if geometry was modified since last call to this method.
	 */
	boolean needsGeometryUpdate();
}
