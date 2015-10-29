package ch.fhnw.ether.formats.obj;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry.IGeometryAttribute;
import ch.fhnw.util.math.Vec3;

public class OBJWriter {
	private final List<IMesh>     meshes = new ArrayList<>();
	private final PrintWriter     out;
	private final WavefrontObject obj;

	public OBJWriter(File file) throws FileNotFoundException {
		this.out  = new PrintWriter(file);
		this.obj  = new WavefrontObject(file.getAbsolutePath());
	}

	public void addMesh(IMesh mesh) {
		meshes.add(mesh);
		Group g = new Group(mesh.getName());
		obj.setCurrentGroup(g);
		IGeometry geometry = mesh.getGeometry();
		geometry.inspect(0, (IGeometryAttribute attribute, float[] data) -> {
			switch (geometry.getType()) {
			case LINES:
				break;
			case POINTS:
				break;
			case TRIANGLES:
				final List<Vec3> vs = obj.getVertices();
				for(int i = 0; i < data.length; i += 9) {
					int[] vi = new int[3];
					vs.add(new Vec3(data[i+0],data[i+1],data[i+2])); vi[0] = vs.size();
					vs.add(new Vec3(data[i+3],data[i+4],data[i+5])); vi[1] = vs.size();
					vs.add(new Vec3(data[i+6],data[i+7],data[i+8])); vi[2] = vs.size();
					g.addFace(new Face(vi, null, null));
				}
				break;
			}
		});
	}

	public void write() {
		obj.write(out);
		out.close();
	}
}
