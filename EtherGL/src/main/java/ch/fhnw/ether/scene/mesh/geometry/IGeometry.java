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

package ch.fhnw.ether.scene.mesh.geometry;

import ch.fhnw.ether.scene.attribute.AbstractAttribute;
import ch.fhnw.ether.scene.attribute.ITypedAttribute;
import ch.fhnw.util.IUpdateRequester;

public interface IGeometry extends IUpdateRequester {
	interface IGeometryAttribute extends ITypedAttribute<float[]> {
		int getNumComponents();
	}

	final class GeometryAttribute extends AbstractAttribute<float[]> implements IGeometryAttribute {
		private final int numComponents;

		public GeometryAttribute(String id, int numComponents) {
			super(id);
			this.numComponents = numComponents;
		}

		@Override
		public int getNumComponents() {
			return numComponents;
		}
	}

	@FunctionalInterface
	interface IAttributeVisitor {
		/**
		 * Inspect or modify a specific attribute of a geometry.
		 */
		void visit(IGeometryAttribute attribute, float[] data);
	}

	@FunctionalInterface
	interface IAttributesVisitor {
		/**
		 * Inspect or modify attributes of a geometry through visitor. Note that
		 * in the current implementation, the attributes must not be changed,
		 * otherwise the mesh will result in an undefined state. It is however
		 * ok to replace all attribute data arrays with new arrays, e.g. of
		 * different size.
		 */
		void visit(IGeometryAttribute[] attributes, float[][] data);
	}

	enum Primitive {
		POINTS, LINES, TRIANGLES;
	}

	// default geometry attributes

	// position array (note that this attribute is mandatory)
	IGeometryAttribute POSITION_ARRAY = new GeometryAttribute("builtin.material.position_array", 3);

	// non-shaded objects
	IGeometryAttribute COLOR_ARRAY = new GeometryAttribute("builtin.material.color_array", 4);

	// texture maps
	IGeometryAttribute COLOR_MAP_ARRAY = new GeometryAttribute("builtin.material.color_map_array", 2);

	// triangles only: normals & shading
	IGeometryAttribute NORMAL_ARRAY = new GeometryAttribute("builtin.material.normal_array", 3);

	// lines only: line width
	IGeometryAttribute LINE_WIDTH_ARRAY = new GeometryAttribute("builtin.material.line_width_array", 1);

	// points only: point size
	IGeometryAttribute POINT_SIZE_ARRAY = new GeometryAttribute("builtin.material.point_size_array", 1);

	/**
	 * Obtain primitive type of this geometry.
	 * 
	 * @return primitive type of this geometry
	 */
	Primitive getType();

	/**
	 * Inspect specific attribute of this geometry through visitor.
	 * 
	 * @param index
	 *            index of attribute to be visited
	 * @param visitor
	 *            attribute visitor used for inspection
	 */
	void inspect(int index, IAttributeVisitor visitor);

	/**
	 * Inspect all attributes of this geometry through visitor.
	 * 
	 * @param visitor
	 *            attributes visitor used for inspection
	 */
	void inspect(IAttributesVisitor visitor);

	/**
	 * Modify specific attribute of this geometry through visitor. If geometry
	 * is modified, {@link #updateRequest()} needs to be called explicitly.
	 * 
	 * @param index
	 *            index of attribute to be visited
	 * @param visitor
	 *            attribute visitor used for modification
	 * 
	 * @throws UnsupportedOperationException
	 *             if geometry cannot be modified.
	 */
	void modify(int index, IAttributeVisitor visitor);

	/**
	 * Modify all attributes of this geometry through visitor. If geometry is
	 * modified, {@link #updateRequest()} needs to be called explicitly.
	 * 
	 * @param visitor
	 *            attributes visitor used for modification
	 * 
	 * @throws UnsupportedOperationException
	 *             if geometry cannot be modified.
	 */
	void modify(IAttributesVisitor visitor);

	/**
	 * Request update after this geometry has been modified
	 */
	void updateRequest();
}
