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

package ch.fhnw.ether.mapping;


public class BoxCalibrationModel implements ICalibrationModel {
	private float boxExtentX;
	private float boxExtentY;
	private float boxExtentZ;
	private float planeExtentX;
	private float planeExtentY;

	private float[] lines;
	private float[] points;

	public BoxCalibrationModel(float boxExtentX, float boxExtentY, float boxExtentZ, float planeExtentX, float planeExtentY) {
		this.boxExtentX = boxExtentX;
		this.boxExtentY = boxExtentY;
		this.boxExtentZ = boxExtentZ;
		this.planeExtentX = planeExtentX;
		this.planeExtentY = planeExtentY;
		lines = getLines();
		points = getPoints();
	}

	@Override
	public float[] getCalibrationLines() {
		return lines;
	}

	@Override
	public float[] getCalibrationPoints() {
		return points;
	}

	private float[] getPoints() {
		float bx = boxExtentX / 2;
		float by = boxExtentY / 2;
		float bz = boxExtentZ;
		float px = planeExtentX / 2;
		float py = planeExtentY / 2;
		return new float[] {
				// box bottom
				bx, by, 0, -bx, by, 0, -bx, -by, 0, bx, -by, 0,
				// box top
				bx, by, bz, -bx, by, bz, -bx, -by, bz, bx, -by, bz,
				// plane
				px, py, 0, -px, py, 0, -px, -py, 0, px, -py, 0 };
	}

	private float[] getLines() {
		float bx = boxExtentX / 2;
		float by = boxExtentY / 2;
		float bz = boxExtentZ;
		return new float[] {
				// bottom
				bx, by, 0, -bx, by, 0, -bx, by, 0, -bx, -by, 0, -bx, -by, 0, bx, -by, 0, bx, -by, 0, bx, by, 0,
				// top
				bx, by, bz, -bx, by, bz, -bx, by, bz, -bx, -by, bz, -bx, -by, bz, bx, -by, bz, bx, -by, bz, bx, by, bz,
				// side
				bx, by, 0, bx, by, bz, -bx, by, 0, -bx, by, bz, -bx, -by, 0, -bx, -by, bz, bx, -by, 0, bx, -by, bz };
	}
}
