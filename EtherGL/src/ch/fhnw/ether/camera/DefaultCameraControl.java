/*
 * Copyright (c) 2013 - 2014 Stefan Muller Arisona, Simon Schubiger, Samuel von Stachelski
 * Copyright (c) 2013 - 2014 FHNW & ETH Zurich
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

package ch.fhnw.ether.camera;

import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;

public class DefaultCameraControl {
	private static final boolean KEEP_ROT_X_POSITIVE = true;
	private static final float MIN_DISTANCE = 0.01f;

	private final ICamera camera;

	public DefaultCameraControl(ICamera camera) {
		this.camera = camera;
	}

	public ICamera getCamera() {
		return camera;
	}

	// convenience methods for basic view parameters

	public void setPosition(Vec3 position) {
		camera.setPosition(position);
	}

	public void setTarget(Vec3 target) {
		camera.setTarget(target);
	}

	public void setUp(Vec3 up) {
		camera.setUp(up);
	}

	// track, pan, dolly, roll (in camera coordinate system, updates target)

	public void track(float deltaX, float deltaY) {
		Vec3 d = getCameraXAxis().scale(deltaX).add(getCameraYAxis().scale(deltaY));
		camera.setPosition(camera.getPosition().add(d));
		camera.setTarget(camera.getTarget().add(d));
	}

	public void pan(float deltaX, float deltaY) {
		throw new UnsupportedOperationException();
	}
	
	public void dolly(float deltaZ) {
		throw new UnsupportedOperationException();		
	}

	public void roll(float deltaZ) {
		throw new UnsupportedOperationException();
	}

	// orbiting mode with respect to X-Y plane (keeps target in position)

	/**
	 * Orbit around target with world-z axis
	 * @param delta
	 */
	public void addToAzimuth(float delta) {
		Mat4 m = Mat4.identityMatrix().translate(camera.getTarget().negate()).rotate(delta, Vec3.Z).translate(camera.getTarget());
		
		Vec3 p = m.transform(camera.getPosition());
		Vec3 u = m.transform(camera.getPosition().add(camera.getUp())).subtract(p);
		
		camera.setPosition(p);
		camera.setUp(u);
	}

	/**
	 * Orbit around target on camera-x axis
	 * @param delta
	 */
	public void addToElevation(float delta) {
		Mat4 m = Mat4.identityMatrix().translate(camera.getTarget().negate()).rotate(delta, getCameraXAxis()).translate(camera.getTarget());
		
		Vec3 p = m.transform(camera.getPosition());
		Vec3 u = m.transform(camera.getPosition().add(camera.getUp())).subtract(p);
		
		if (KEEP_ROT_X_POSITIVE && Vec3.Z.dot(u) < 0)
			return;
		
		camera.setPosition(p);
		camera.setUp(u);
	}

	public float getDistance() {
		Vec3 p = camera.getPosition();
		Vec3 t = camera.getTarget();
		return p.distance(t);
	}
	
	public void setDistance(float distance) {
		Vec3 t = camera.getTarget();
		Vec3 z = getCameraZAxis().scale(distance);
		camera.setPosition(t.add(z));
	}

	public void addToDistance(float delta) {
		Vec3 p = camera.getPosition();
		Vec3 t = camera.getTarget();
		Vec3 z = getCameraZAxis().scale(delta);
		p = p.add(z);
		if (delta < 0 && (p.distance(t) < MIN_DISTANCE || p.subtract(t).dot(z) > 0))
			setDistance(MIN_DISTANCE);	
		else
			camera.setPosition(p);
	}
	
	private Vec3 getCameraXAxis() {
		Vec3 u = camera.getUp();
		Vec3 z = getCameraZAxis();
		return u.cross(z).normalize();
	}
	
	private Vec3 getCameraYAxis() {
		Vec3 x = getCameraXAxis();
		Vec3 z = getCameraZAxis();
		return z.cross(x).normalize();
	}

	private Vec3 getCameraZAxis() {
		Vec3 p = camera.getPosition();
		Vec3 t = camera.getTarget();
		return p.subtract(t).normalize();
	}
}
