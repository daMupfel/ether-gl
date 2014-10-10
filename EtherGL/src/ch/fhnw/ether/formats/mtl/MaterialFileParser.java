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

package ch.fhnw.ether.formats.mtl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import ch.fhnw.ether.formats.obj.LineParser;
import ch.fhnw.ether.formats.obj.Material;
import ch.fhnw.ether.formats.obj.WavefrontObject;

public class MaterialFileParser extends LineParser {

	Hashtable<String, Material> materials = new Hashtable<String, Material>();
	private WavefrontObject object;
	private MtlLineParserFactory parserFactory = null;

	public MaterialFileParser(WavefrontObject object) {
		this.object = object;
		this.parserFactory = new MtlLineParserFactory(object);
	}

	@Override
	public void incoporateResults(WavefrontObject wavefrontObject) {
		// Material are directly added by the parser, no need to do anything here...
	}

	@Override
	public void parse() {
		String filename = words[1];

		String pathToMTL = object.getContextfolder() + filename;

		InputStream fileInput = this.getClass().getResourceAsStream(pathToMTL);
		if (fileInput == null)
			// Could not find the file in the jar.
			try {
				File file = new File(pathToMTL);
				if (file.exists())
					fileInput = new FileInputStream(file);
			} catch (Exception e2) {
				e2.printStackTrace();
			}

		if (fileInput == null)
			return;
		String currentLine = null;
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(fileInput));

			currentLine = null;
			while ((currentLine = in.readLine()) != null) {

				LineParser parser = parserFactory.getLineParser(currentLine);
				parser.parse();
				parser.incoporateResults(object);
			}

			if (in != null)
				in.close();

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Error on line:" + currentLine);
			throw new RuntimeException("Error parsing :'" + pathToMTL + "'");
		}

	}

}