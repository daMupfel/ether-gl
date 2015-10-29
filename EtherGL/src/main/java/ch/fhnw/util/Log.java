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

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Date;

public class Log implements Serializable {
	enum Level {
		SEVERE, WARN, INFO,
	}
	
	private static final long serialVersionUID = -4288206500724445427L;
	private final transient PrintStream out = System.err;
	private final transient String      id;  
	
	private Log(String id) {
		this.id = id;
	}
	
	public static Log create() {
		return new Log(ClassUtilities.getCallerClassName());
	}

	private String format(Level lvl, String msg) {
		return new Date() + ":" + lvl.toString() + '(' + id + ')' + ':' + msg;
	}
	
	public void info(String msg) {
		out.println(format(Level.INFO, msg));
	}

	public void info(String msg, Throwable t) {
		out.println(format(Level.INFO, msg));
		t.printStackTrace(out);
	}

	public void warning(Throwable t) {
		out.print(format(Level.WARN, ClassUtilities.EMPTY_String));
		t.printStackTrace(out);
	}

	public void warning(String msg) {
		out.println(format(Level.WARN, msg));
	}

	public void warning(String msg, Throwable t) {
		out.println(format(Level.WARN, msg));
		t.printStackTrace(out);
	}

	public void severe(Throwable t) {
		out.println(format(Level.SEVERE, ClassUtilities.EMPTY_String));
		t.printStackTrace(out);
	}
}
