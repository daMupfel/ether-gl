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

package ch.fhnw.util;

import java.util.Arrays;

public final class SortedIntSet {
	private int[] set;
	private int   size;

	public SortedIntSet() {
		this(4);
	}

	public SortedIntSet(int size) {
		set = new int[size];
	}

	public SortedIntSet(int[] values) {
		set  = values.clone();
		size = values.length;
		Arrays.sort(set);
	}

	public boolean add(int value) {
		int idx = Arrays.binarySearch(set, 0, size, value);
		if(idx < 0) {
			idx = -idx - 1;
			if(size >= set.length)
				set = Arrays.copyOf(set, set.length  * 2);

			int count = size -  idx;
			if(count > 0)
				System.arraycopy(set, idx, set, idx + 1, count);
			set[idx] = value;
			size++;
			return true;
		}
		return false;
	}

	public void remove(int value) {
		int idx = Arrays.binarySearch(set, 0, size, value);
		if(idx >= 0) {
			int count = (size -  idx) - 1;
			if(count > 0)
				System.arraycopy(set, idx + 1, set, idx, count);
			size--;
		}
	}

	public boolean contains(int value) {
		return Arrays.binarySearch(set, 0, size, value) >= 0;
	}

	public int size() {
		return size;
	}

	public int[] sorted() {
		return Arrays.copyOf(set, size);
	}
	
	@Override
	public String toString() {
		return TextUtilities.toString("{", ",", "}", set, TextUtilities.NONE, 0, size);
	}

	public int[] toArray() {
		return sorted();
	}

	public void clear() {
		if (size<4)
			set  = new int[4];
		else
			set  = new int[size];
		size = 0;
	}
} 
