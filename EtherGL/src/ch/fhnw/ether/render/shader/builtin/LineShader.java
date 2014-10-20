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

package ch.fhnw.ether.render.shader.builtin;

import ch.fhnw.ether.render.attribute.IAttribute;
import ch.fhnw.ether.render.attribute.base.BooleanUniformAttribute;
import ch.fhnw.ether.render.attribute.builtin.ColorArray;
import ch.fhnw.ether.render.attribute.builtin.ColorMaterialUniform;
import ch.fhnw.ether.render.attribute.builtin.PositionArray;
import ch.fhnw.ether.render.attribute.builtin.ProjMatrixUniform;
import ch.fhnw.ether.render.attribute.builtin.ViewMatrixUniform;
import ch.fhnw.ether.render.shader.IShader;
import ch.fhnw.ether.render.shader.base.AbstractShader;

public class LineShader extends AbstractShader {
	public LineShader(boolean useVertexColors) {
		super(IShader.class, "builtin.lines", "unshaded_vct", IAttribute.PrimitiveType.LINE);

		if (!useVertexColors) {
			addUniform(new ColorMaterialUniform());
		} else {
			addArray(new ColorArray());
		}

		addArray(new PositionArray());
		addUniform(new ProjMatrixUniform());
		addUniform(new ViewMatrixUniform());
		addUniform(new BooleanUniformAttribute("shader.vertex_colors_flag", "useVertexColors", () -> useVertexColors));
		addUniform(new BooleanUniformAttribute("shader.texture_flag", "useTexture", () -> false));
	}
}
