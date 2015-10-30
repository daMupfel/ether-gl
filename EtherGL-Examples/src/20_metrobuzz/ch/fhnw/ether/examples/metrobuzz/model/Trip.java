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
 */package ch.fhnw.ether.examples.metrobuzz.model;

import java.util.ArrayList;
import java.util.List;

public class Trip {
	public enum TripMode {
		WALK, TRANSIT_WALK, CAR, PT, UNDEFINED
	}

	private TripMode mode;
	private float startTime;
	private float endTime;
	private List<Link> links = new ArrayList<>();

	public Trip(TripMode mode, float startTime, float endTime) {
		this.mode = mode;
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public TripMode getMode() {
		return mode;
	}

	public void setMode(TripMode mode) {
		this.mode = mode;
	}

	public float getStartTime() {
		return startTime;
	}

	public void setStartTime(float time) {
		this.startTime = time;
	}

	public float getEndTime() {
		return endTime;
	}

	public void setEndTime(float time) {
		this.endTime = time;
	}

	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(List<Link> links) {
		this.links = links;
	}

	public Node getFromNode() {
		return links.get(0).getFromNode();
	}

	public Node getToNode() {
		return links.get(links.size() - 1).getToNode();
	}

	@Override
	public String toString() {
		return "trip (" + mode.toString() + ") " + startTime + " " + endTime + " (" + links.size() + ")";
	}
}
