package org.openjump.tin.io;

import java.lang.String;
import java.nio.ByteOrder;

/**
 * This class contains constants and documentation describing the layout of a 
 * binary JTF file.
 * <p>
 * <h1>File Format: .JTF</h1>
 * <p>
 * JTF stands for "JUMP TIN Format." It is a simple, efficient, geospatially 
 * aware standard file format for Triangular Irregular Networks (TINs). This 
 * format was initially designed to work with OpenJUMP/JTIN and allow for easy 
 * analysis and display.
 * <p>
 * <h1>Current format specification: JTF Version 1.0</h1>
 * <p>
 * The file begins with a header, containing the following:
 * <p>
 * <ul>
 * <li>Field	Length in Bytes	Description
 * <li>File_Marker	6	A marker which indicates that this is a JTF file. For 
 * version 1.0 of the format, it contains the characters "jtin01"
 * <li>Num_Vertices	4 (int)	Number of vertices in the TIN.
 * <li>Num_Triangles	4 (int)	Number of triangles that compose the TIN.
 * <li>Num_Breaklines	4 (int)	Number of breaklines present.
 * <li>Num_Boundaries	4 (int)	Number of boundaries present.
 * <li>Data_Start	4 (int)	File offset where the data starts. A parser should 
 * seek to this position to read it
 * <li>SRID	4 (int)	EPSG code representing the spatial ID for this TIN.
 * </ul>
 * <p>
 * Next is the data itself, vertices, then triangles, then connections between 
 * the triangles, then breaklines, then finally boundaries.
 * <p>
 * Each vertex is encoded using the following format:
 * <p>
 * <ul>
 * <li>Field	Length	Description
 * <li>X	8 (double)	X coordinate (easting)
 * <li>Y	8 (double)	Y coordinate (northing)
 * <li>Z	8 (double)	Z coordinate (elevation) Note: this differs from the 
 * ITF spec by using a double rather than a float in order to better fit the 
 * Java Topology Suite.
 * </ul>
 * <p>
 * Next are the triangles, which each contain the array indices of their three 
 * vertices (index corresponds to the above array of vertices) and the indices 
 * of the three neighboring triangles (index corresponds to this array of 
 * triangles). Vertices should be listed in clockwise direction when viewed 
 * from above the surface. Neighboring triangles should be listed in clockwise 
 * ordering starting with the triangle opposite the point Vertex_Idx1:
 * <p>
 * <ul>
 * <li>Field	Length	Description
 * <li>Vertex_Idx1	4 (int)	First corner.
 * <li>Vertex_Idx2	4 (int)	Second corner.
 * <li>Vertex_Idx3	4 (int)	Third corner.
 * <li>Triangle_Idx1	4 (int)	Index of neighboring triangle opposite Vertex_Idx1
 * <li>Triangle_Idx2	4 (int)	Index of neighboring triangle opposite Vertex_Idx2
 * <li>Triangle_Idx3	4 (int)	Index of neighboring triangle opposite Vertex_Idx3
 * </ul>
 * <p>
 * Next is a list of breaklines. Each breakline entry begins with the number of 
 * vertices composing the breakline followed by that many indices to the vertex 
 * table. Vertexes should be listed in order. If the breakline forms a ring, 
 * the first and last vertex indices should be equal. 
 * <p>
 * This data section is optional and may not be included for a given TIN. In 
 * that case Num_Breaklines in the header will be equal to zero.
 * <p>
 * <ul>
 * <li>Field	Length	Description
 * <li>Breakline_Length	4 (int)	Number of points composing the breakline.
 * <li>Vertex_Idx1	4 (int)	First vertex of the breakline.
 * <li>...
 * <li>Vertex_IdxN	4 (int)	Last vertex: N = Breakline_Length
 * </ul>
 * <p>
 * The final data section is a list of boundary lines. They follow the same 
 * format as the Breakline table above.
 * <p>
 * <ul>
 * <li>Field	Length	Description
 * <li>Boundary_Length	4 (int)	Number of points composing the boundary.
 * <li>Vertex_Idx1	4 (int)	First vertex of the boundary.
 * <li>...
 * <li>Vertex_IdxN	4 (int)	Last vertex: N = Boundary_Length
 * </ul>
 * 
 * @author Christopher DeMars
 *
 */
public class JTFLayout {

	public final static String FILE_MARKER = "jtin01";
	public final static int MARKER_BYTE_SIZE = FILE_MARKER.length() * Character.SIZE / Byte.SIZE;
	public final static int NUM_HEADER_INT_FIELDS = 6;
	public final static int NUM_COORDINATES_PER_VERTEX = 3;
	public final static int NUM_TRIANGLE_INT_FIELDS = 6;
	public final static String FILE_NAME_EXTENSION = "jtf";
	public final static ByteOrder BYTE_ORDER = ByteOrder.BIG_ENDIAN;
	public final static String CHAR_SET = "UTF-16BE";

	// format for triangle table arrangement. Value is equal to array index
	// in the tritable.
	public final static int TRITABLE_VERTEX_0 = 0;
	public final static int TRITABLE_VERTEX_1 = 1;
	public final static int TRITABLE_VERTEX_2 = 2;
	public final static int TRITABLE_NEIGHBOR_0 = 3;
	public final static int TRITABLE_NEIGHBOR_1 = 4;
	public final static int TRITABLE_NEIGHBOR_2 = 5;
	

	
	
	public JTFLayout(){}
}
