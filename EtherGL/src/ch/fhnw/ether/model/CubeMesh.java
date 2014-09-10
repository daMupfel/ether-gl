/*
 * Copyright (c) 2013 - 2014 FHNW & ETH Zurich (Stefan Muller Arisona & Simon Schubiger)
 * Copyright (c) 2013 - 2014 Stefan Muller Arisona & Simon Schubiger
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

package ch.fhnw.ether.model;

import ch.fhnw.ether.geom.Vec3;
import ch.fhnw.ether.render.util.Primitives;

/**
 * Created by radar on 05/12/13.
 */
public final class CubeMesh extends GenericMesh {
    public enum Origin {
        CENTER(Vec3.ZERO),
        BOTTOM_CENTER(new Vec3(0, 0, -0.5)),
        ZERO(new Vec3(-0.5, -0.5, -0.5));

        Origin(Vec3 origin) {
            this.origin = origin;
        }

        Vec3 origin;
    }

    public CubeMesh() {
        this(Origin.CENTER);
    }

    public CubeMesh(Origin origin) {
        this(origin.origin);
    }

    public CubeMesh(Vec3 origin) {
        super(origin);
        setTriangles(Primitives.UNIT_CUBE_TRIANGLES);
        setLines(Primitives.UNIT_CUBE_EDGES);
        setPoints(Primitives.UNIT_CUBE_POINTS);
    }
}