package org.openjump.tin.io;

import org.openjump.tin.TriangulatedIrregularNetwork;
import org.openjump.tin.io.JTFLayout;

import com.vividsolutions.jts.geom.Coordinate;

import java.io.OutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Given an output stream and a TriangulatedIrregularNetwork, JTFReader.write() 
 * will write out the tin to the file in a binary format.
 * 
 * @author Christopher DeMars
 *
 */
public class JTFWriter {

	public JTFWriter(){
	}
	
	/**
	 * Method to convert a TriangulatedIrregularNetwork into a binary stream
	 * in accordance with JTFLayout.
	 * 
	 * @param tin			The TIN to write out
	 * @param out			The stream to write to
	 * @throws IOException
	 * @see JTFLayout
	 * @see JTFReader
	 */
	public static void write(TriangulatedIrregularNetwork tin, OutputStream out) throws IOException {
		
		byte[] tmpByteArray;
		ByteBuffer bbuf;
		int tmpByteArraySize;
		Coordinate[] vertices;
		int[] triangle;
		
		try {
			// write file marker
			out.write(JTFLayout.FILE_MARKER.getBytes(JTFLayout.CHAR_SET));
			
			// write the header int fields
			tmpByteArraySize = JTFLayout.NUM_HEADER_INT_FIELDS * Integer.SIZE  / Byte.SIZE;
			tmpByteArray = new byte[tmpByteArraySize]; 
			bbuf = ByteBuffer.wrap(tmpByteArray).order(JTFLayout.BYTE_ORDER);
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
			bbuf = ByteBuffer.wrap(tmpByteArray).order(JTFLayout.BYTE_ORDER);
			for (int i=0; i<vertices.length; i++) {
				bbuf.putDouble(vertices[i].x);
				bbuf.putDouble(vertices[i].y);
				bbuf.putDouble(vertices[i].z);
			}
			out.write(tmpByteArray);
			
			// write the triangle table
			tmpByteArraySize = tin.getNumTriangles() * JTFLayout.NUM_TRIANGLE_INT_FIELDS * Integer.SIZE / Byte.SIZE;
			tmpByteArray = new byte[tmpByteArraySize]; 
			bbuf = ByteBuffer.wrap(tmpByteArray).order(JTFLayout.BYTE_ORDER);			
			for (int i=0; i < tin.getNumTriangles(); i++) {
				triangle = tin.getTriangleArrayN(i);
				for (int j=0; j < triangle.length; j++)
					bbuf.putInt(triangle[j]);
			}
			out.write(tmpByteArray);
			
			// write the breaklines
			if (tin.getNumBreaklines() > 0)
				for (int[] line : tin.getBreaklines()) {
					writeLine(out, line);
				}
			
			// write the Boundaries
			if (tin.getNumBoundaries() > 0)
			for (int[] line : tin.getBoundaries()) {
				writeLine(out, line);
			}

		}
		finally {
			out.flush();
			out.close();
		}
	}
	
	
	/**
	 * Accessory method to be used with both breaklines and boundaries.
	 * 
	 * @param out	the output stream. First integer of the stream is the number
	 * 				of following points. Each point is an integer that represents
	 * 				an index to an external array of points.
	 * @return 		an array of integers describing the line
	 */	
	private static void writeLine (OutputStream out, int[] line) throws IOException {
		byte[] tmpByteArray;
		ByteBuffer bbuf;
		int tmpByteArraySize;
		
		tmpByteArraySize = (line.length + 1) * Integer.SIZE / Byte.SIZE;
		tmpByteArray = new byte[tmpByteArraySize]; 
		bbuf = ByteBuffer.wrap(tmpByteArray).order(JTFLayout.BYTE_ORDER);	
		// write out length of the line first
		bbuf.putInt(line.length);
		// write out the line
		for (int i=0; i<line.length; i++) {
			bbuf.putInt(line[i]);
		}
		
		out.write(tmpByteArray);
	}
}
