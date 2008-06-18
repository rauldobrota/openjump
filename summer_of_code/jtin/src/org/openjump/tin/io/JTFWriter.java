package org.openjump.tin.io;

import org.openjump.tin.TriangulatedIrregularNetwork;
import org.openjump.tin.io.JTFLayout;

import com.vividsolutions.jts.geom.Coordinate;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class JTFWriter {

	public JTFWriter(){
	}
	
	public static void write(TriangulatedIrregularNetwork tin, OutputStream out) throws IOException {
		
		byte[] tmpByteArray;
		ByteBuffer bbuf;
		int tmpByteArraySize;
		Coordinate[] vertices;
		int[] triangle;
		
		try {
			// write file marker
			out.write(JTFLayout.FILE_MARKER.getBytes("UTF-16BE"));
			//System.out.println("JTFWriter: file marker = "+new String(JTFLayout.FILE_MARKER.getBytes()));
			
			// write the header int fields
			tmpByteArraySize = JTFLayout.NUM_HEADER_INT_FIELDS * Integer.SIZE  / Byte.SIZE;
			tmpByteArray = new byte[tmpByteArraySize]; 
			bbuf = ByteBuffer.wrap(tmpByteArray);
			bbuf.putInt(tin.getNumVertices());
			bbuf.putInt(tin.getNumTriangles());
			bbuf.putInt(tin.getNumBreaklines());
			bbuf.putInt(tin.getNumBoundaries());
			bbuf.putInt(JTFLayout.MARKER_BYTE_SIZE + tmpByteArraySize); // datastart
			bbuf.putInt(tin.getSRID());
			out.write(tmpByteArray);

			// write the vertices
			vertices = tin.getVertices();
			tmpByteArraySize = vertices.length * JTFLayout.NUM_COORDINATES_PER_VERTEX * Double.SIZE / Byte.SIZE;
			tmpByteArray = new byte[tmpByteArraySize];  
			bbuf = ByteBuffer.wrap(tmpByteArray);
			for (int i=0; i<vertices.length; i++) {
				bbuf.putDouble(vertices[i].x);
				bbuf.putDouble(vertices[i].y);
				bbuf.putDouble(vertices[i].z);
			}
			out.write(tmpByteArray);
			
			// write the triangle table
			tmpByteArraySize = tin.getNumTriangles() * JTFLayout.NUM_TRIANGLE_INT_FIELDS * Integer.SIZE / Byte.SIZE;
			tmpByteArray = new byte[tmpByteArraySize]; 
			bbuf = ByteBuffer.wrap(tmpByteArray);			
			for (int i=0; i < tin.getNumTriangles(); i++) {
				triangle = tin.getTriangleArrayN(i);
				for (int j=0; j < triangle.length; j++)
					bbuf.putInt(triangle[j]);
			}
			out.write(tmpByteArray);
			
			// write the breaklines
			for (int[] line : tin.getBreaklines()) {
				writeLine(out, line);
			}
			
			// write the Boundaries
			for (int[] line : tin.getBoundaries()) {
				writeLine(out, line);
			}

		}
		finally {
			out.flush();
			out.close();
		}
	}
	
	
	private static void writeLine (OutputStream out, int[] line) throws IOException {
		byte[] tmpByteArray;
		ByteBuffer bbuf;
		int tmpByteArraySize;
		
		tmpByteArraySize = (line.length + 1) * Integer.SIZE / Byte.SIZE;
		tmpByteArray = new byte[tmpByteArraySize]; 
		bbuf = ByteBuffer.wrap(tmpByteArray);	
		// write out length of the line first
		bbuf.putInt(line.length);
		// write out the line
		for (int i=0; i<line.length; i++) {
			bbuf.putInt(line[i]);
		}
		
		out.write(tmpByteArray);
	}
}
