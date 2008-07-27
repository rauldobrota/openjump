package org.openjump.tin.io;

import java.util.List;
import java.util.ArrayList;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.openjump.tin.TriangulatedIrregularNetwork;
import org.openjump.tin.ImmutableTin;
import org.openjump.tin.io.JTFLayout;
import org.openjump.tin.io.JTFReader;
import org.openjump.tin.io.JTFWriter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;


public class JTFReaderWriterTester {
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String filename = args[0];
		
		 Coordinate[] points = new Coordinate[16];
		 int[][] triTable = new int[18][6];
		 List<int[]> breaklines = new ArrayList<int[]>(1);
		 List<int[]> boundaries = new ArrayList<int[]>(1);
		 int spatialID = 1234;
		 ImmutableTin tin1, tin2;
		 
		 FileInputStream fin;
		 FileOutputStream fout;
		 
		 /* Test tin
		  * 3x3 square grid with each square bisected by a line from the upper
		  * left to lower right corners. (0,0,0) is located on the lower left
		  * corner, (300,300,300) is located at the upper right corner while
		  * the upper left and lower right corners are (0,300,15) and 
		  * (300,0,150) respectively.
		  * The border runs around the full square and the boundary runs from
		  * the upper left to the lower right of the full square.
		  * Neighbor indexes for triangles bordering the boundary are set to -1
		  */
		 points[0] = new Coordinate(0, 300, 150);
		 points[1] = new Coordinate(100, 300, 200);
		 points[2] = new Coordinate(200, 300, 250);
		 points[3] = new Coordinate(300, 300, 300);
		 points[4] = new Coordinate(0, 200, 100);
		 points[5] = new Coordinate(100, 200, 150);
		 points[6] = new Coordinate(200, 200, 200);
		 points[7] = new Coordinate(300, 200, 250);
		 points[8] = new Coordinate(0, 100, 50);
		 points[9] = new Coordinate(100, 100, 100);
		 points[10] = new Coordinate(200, 100, 150);
		 points[11] = new Coordinate(300, 100, 200);
		 points[12] = new Coordinate(0, 0, 0);
		 points[13] = new Coordinate(100, 0, 50);
		 points[14] = new Coordinate(200, 0, 100);
		 points[15] = new Coordinate(300, 0, 150);
		 
		 triTable[0] = new int[] {0,1,5,4,3,-1 };
		 triTable[1] = new int[] {1,2,6,5,4,-1 };
		 triTable[2] = new int[] {2,3,7,-1,5,-1 };
		 triTable[3] = new int[] {0,5,4,6,-1,0 };
		 triTable[4] = new int[] {1,6,5,7,0,1 };
		 triTable[5] = new int[] {2,7,6,8,1,2 };
		 triTable[6] = new int[] {4,5,9,10,9,3 };
		 triTable[7] = new int[] {5,6,10,11,10,4 };
		 triTable[8] = new int[] {6,7,11,-1,11,5 };
		 triTable[9] = new int[] {4,9,8,12,-1,6 };
		 triTable[10] = new int[] {5,10,9,13,6,7 };
		 triTable[11] = new int[] {6,11,10,14,7,8 };
		 triTable[12] = new int[] {8,9,13,16,15,9 };
		 triTable[13] = new int[] {9,10,14,17,16,10 };
		 triTable[14] = new int[] {10,11,15,-1,17,11 };
		 triTable[15] = new int[] {8,13,12,-1,-1,12 };
		 triTable[16] = new int[] {9,14,13,-1,12,13 };
		 triTable[17] = new int[] {10,15,14,-1,13,14 };

		 breaklines.add(new int[] {0,5,10,15});
		 boundaries.add(new int[] {0,1,2,3,7,11,15,14,13,12,8,4,0});
		 
		 tin1 = new ImmutableTin(new CoordinateArraySequence(points), triTable, breaklines, boundaries, spatialID);
		 
		 // see what the tin thinks it is
		 System.out.println("Hard Coded tin = \n" + tin1.toString() + "\n\n");
		 
		 // write tin to file
		 System.out.println("\nWriting tin to: "+filename+"....\n");
		 try {
			 fout = new FileOutputStream(filename);
			 JTFWriter.write(tin1, fout);
			 fout.close();
		 }
		 catch (IOException e) {
			 System.out.println("Error writing tin file " + filename +": "+ e.toString() + "\n");
			 return;
		 }
		 
		 // open tin file
		 System.out.println("\nReading tin from: "+filename+"....\n");
		 try {
			 fin = new FileInputStream(filename);
		 }
		 catch (IOException e){
			System.out.println("Error opening tin file " + filename + ": " + e.toString() + "\n");
			return;
		 }

		 // read tin from file
		 try {
			 tin2 = JTFReader.read(fin);
			 fin.close();
		 }
		 catch (IOException e){
			System.out.println("Error reading tin file " + filename + ": " + e.toString() + "\n");
			return;
		 }
		 
		 // see what the second tin thinks it is
		 System.out.println("Re-Read in tin = \n" + tin2.toString() + "\n\n");

		 return;
	}

}
