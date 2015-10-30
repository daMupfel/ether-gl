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

package ch.fhnw.ether.examples.metrobuzz.io.matsim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import ch.fhnw.ether.examples.metrobuzz.model.Link;
import ch.fhnw.ether.examples.metrobuzz.model.Trip;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.fhnw.ether.examples.metrobuzz.model.Activity;
import ch.fhnw.ether.examples.metrobuzz.model.Activity.ActivityType;
import ch.fhnw.ether.examples.metrobuzz.model.Agent;
import ch.fhnw.ether.examples.metrobuzz.model.Node;
import ch.fhnw.ether.examples.metrobuzz.model.Scene;

public class Loader {
	private static final boolean DBG = false;

	private SAXParserFactory parserFactory = SAXParserFactory.newInstance();

	public static void load(Scene model, String basePath, int maxAgents) throws IOException {
		new Loader(model, basePath, maxAgents);
		model.normalize();
		model.createGeometries();
	}

	private Loader(Scene model, String basePath, int maxAgents) throws IOException {
		loadNetwork(model, basePath + "/output_network.xml");
		loadAgents(model, basePath + "/output_plans.xml", maxAgents);
		System.out.println("info: loaded network (" + model.getNodes().size() + " nodes, " + model.getLinks().size() + " links, " + model.getAgents().size()
				+ " agents)");
	}

	private void loadNetwork(final Scene model, final String path) throws IOException {
		try {
			SAXParser parser = parserFactory.newSAXParser();
			parser.parse(path, new DefaultHandler() {
				@Override
				public InputSource resolveEntity(String publicId, String systemId) throws org.xml.sax.SAXException, java.io.IOException {
					System.out.println("Ignoring: " + publicId + ", " + systemId);
					return new InputSource(new java.io.StringReader(""));
				}

				@Override
				public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
					if (name.equals("node")) {
						addNode(model, attributes);
					} else if (name.equals("link")) {
						addEdge(model, attributes);
					}
				}
			});
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			if (DBG)
				e.printStackTrace();
		}
	}

	private void addNode(Scene model, Attributes attributes) {
		// attributes: id, x, y
		String id = attributes.getValue("id");
		String x = attributes.getValue("x");
		String y = attributes.getValue("y");

		if (id == null)
			return;

		if (model.getNode(id) == null) {
			try {
				model.addNode(new Node(id, Float.parseFloat(x), Float.parseFloat(y)));
			} catch (NumberFormatException e) {
				System.out.println("warning: ignoring node " + id + " (invalid position)");
			}
		} else {
			System.out.println("warning: ignoring node " + id + " (already existing)");
		}
	}

	private void addEdge(Scene model, Attributes attributes) {
		// attributes: id, fromNode, toNode
		String id = attributes.getValue("id");
		String fromNode = attributes.getValue("from");
		String toNode = attributes.getValue("to");

		if (id == null)
			return;

		if (model.getLink(id) == null) {
			Node from = model.getNode(fromNode);
			Node to = model.getNode(toNode);
			if (from != null && to != null) {
				model.addLink(new Link(id, from, to));
			} else if (from == null) {
				System.out.println("warning: ignoring link " + id + " (from node does not exist)");
			} else if (to == null) {
				System.out.println("warning: ignoring link " + id + " (to node does not exist)");
			}
		} else {
			System.out.println("warning: ignoring link " + id + " (already existing)");
		}
	}

	private enum CollectType {
		NONE, PUBLIC_TRANSPORT, CAR
	}

	private void loadAgents(final Scene model, final String path, final int maxAgents) throws IOException {
		try {
			SAXParser parser = parserFactory.newSAXParser();
			parser.parse(path, new DefaultHandler() {
				Agent agent = null;
				boolean itinerary = false;
				int currentTime = 0;
				Activity activity = null;
				Trip trip = null;
				List<Link> links = null;
				CollectType collectType = CollectType.NONE;

				@Override
				public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
					if (name.equals("person")) {
						// "person"
						s("person");
						String id = attributes.getValue("id");
						agent = new Agent(id);
						currentTime = 0;

						// terminate loading above a preset size
						if (model.getAgents().size() > maxAgents) {
							throw new SAXException("limit reached");
						}
					} else if (name.equals("plan")) {
						// "plan"
						s("plan");
						if (attributes.getValue("selected").equals("yes"))
							itinerary = true;
					} else if (name.equals("act")) {
						// "activity"
						s("act");
						if (itinerary) {
							ActivityType type = getActivityType(attributes.getValue("type"));
							String link = attributes.getValue("link");
							String startTime = attributes.getValue("start_time");
							String endTime = attributes.getValue("end_time");

							if (startTime != null && endTime != null) {
								// regular activity with start and end time
								currentTime = parseTime(endTime);
								activity = new Activity(type, model.getLink(link).getFromNode(), parseTime(startTime), currentTime);
							} else if (startTime != null && endTime == null) {
								// last activity (until end of day)
								activity = new Activity(type, model.getLink(link).getFromNode(), parseTime(startTime), parseTime("23:59:59"));
							} else if (endTime != null) {
								int et = parseTime(endTime);
								activity = new Activity(type, model.getLink(link).getFromNode(), currentTime, et);
								currentTime = et;
							} else {
								// activity without time (such as "pt interaction")
								activity = new Activity(type, model.getLink(link).getFromNode(), currentTime, currentTime);
							}
						}
					} else if (name.equals("leg")) {
						// "leg" = "trip"
						s("leg");
						if (itinerary) {
							assert activity != null;
							Trip.TripMode mode = getTripMode(attributes.getValue("mode"));
							String depTime = attributes.getValue("dep_time");
							String arrTime = attributes.getValue("arr_time");
							String travTime = attributes.getValue("trav_time");
							if (depTime != null && arrTime != null) {
								// leg with departure and arrival time given
								currentTime = parseTime(arrTime);
								trip = new Trip(mode, parseTime(depTime), currentTime);
							} else if (travTime != null) {
								// leg with travel time only given
								int tt = parseTime(travTime);
								trip = new Trip(mode, currentTime, currentTime + tt);
								currentTime += tt;
							} else {
								// no time given (should not happen)
								trip = new Trip(mode, currentTime, currentTime);
							}
						}
					} else if (name.equals("route")) {
						// "route" = a list of links
						s("route");
						if (itinerary) {
							assert trip != null;
							links = new ArrayList<>();
							String type = attributes.getValue("type");
							if (type.equals("generic")) {
								String startLink = attributes.getValue("start_link");
								String endLink = attributes.getValue("end_link");
								links.add(model.getLink(startLink));
								links.add(model.getLink(endLink));
							} else if (type.equals("pt") || type.equals("experimentalPt1")) {
								String startLink = attributes.getValue("start_link");
								String endLink = attributes.getValue("end_link");
								links.add(model.getLink(startLink));
								links.add(model.getLink(endLink));
								collectType = CollectType.PUBLIC_TRANSPORT;
							} else if (type.equals("links")) {
								collectType = CollectType.CAR;
							}
						}
					}
				}

				@Override
				public void characters(char[] ch, int start, int length) throws SAXException {
					switch (collectType) {
					case CAR:
						for (String link : new String(ch, start, length).split(" ")) {
							links.add(model.getLink(link));
						}
						break;
					case PUBLIC_TRANSPORT:
						// currently ignore
						break;
					default:
						// ignore
						break;
					}
				}

				@Override
				public void endElement(String uri, String localName, String name) throws SAXException {
					if (name.equals("person")) {
						e("person");
						if (activity != null) {
							activity.setEndTime(24 * 60 * 60);
							agent.addActivity(activity);
							activity = null;
						}
						model.addAgent(agent);
						agent = null;
					} else if (name.equals("plan")) {
						e("plan");
						itinerary = false;
					} else if (name.equals("act")) {
						e("act");
					} else if (name.equals("leg")) {
						e("leg");
						if (itinerary) {
							activity.setTrip(trip);
							agent.addActivity(activity);
							activity = null;
							trip = null;
						}
					} else if (name.equals("route")) {
						e("route");
						if (itinerary) {
							trip.setLinks(links);
							links = null;
							collectType = CollectType.NONE;
						}
					}
				}

				private void s(String element) {
					if (DBG)
						System.out.println("s: " + element);
				}

				private void e(String element) {
					if (DBG)
						System.out.println("e: " + element);
				}

				@Override
				public InputSource resolveEntity(String publicId, String systemId) throws org.xml.sax.SAXException, java.io.IOException {
					System.out.println("Ignoring: " + publicId + ", " + systemId);
					return new InputSource(new java.io.StringReader(""));
				}
			});
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			if (DBG)
				e.printStackTrace();
		}
	}

	private static ActivityType getActivityType(String type) {
		if (type.equals("home"))
			return ActivityType.HOME;
		if (type.equals("work"))
			return ActivityType.WORK;
		if (type.equals("education"))
			return ActivityType.EDUCATION;
		if (type.equals("leisure"))
			return ActivityType.LEISURE;
		if (type.equals("shop"))
			return ActivityType.SHOP;
		if (type.equals("pt interaction"))
			return ActivityType.PT_INTERACTION;
		return ActivityType.UNDEFINED;
	}

	private static Trip.TripMode getTripMode(String type) {
		if (type.equals("walk"))
			return Trip.TripMode.WALK;
		if (type.equals("transit_walk"))
			return Trip.TripMode.WALK;
		if (type.equals("car"))
			return Trip.TripMode.CAR;
		if (type.equals("pt"))
			return Trip.TripMode.PT;
		return Trip.TripMode.UNDEFINED;
	}

	private static int parseTime(String time) {
		int h = Integer.parseInt(time.substring(0, 2));
		int m = Integer.parseInt(time.substring(3, 5));
		int s = Integer.parseInt(time.substring(6));
		int timeInSec = 3600 * h + 60 * m + s;
		if (timeInSec > 3600 * 24)
			timeInSec -= 3600 * 24;
		return timeInSec;
	}
}
