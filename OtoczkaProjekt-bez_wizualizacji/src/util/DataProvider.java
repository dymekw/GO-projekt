package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import geometry.Point3D;
import geometry.Vertex;

public class DataProvider {
	public LinkedList<Vertex> getPointsFromFile(String fileName) {
		LinkedList<Vertex> result = new LinkedList<>();
		BufferedReader br = null;

		try {

			String sCurrentLine;

			br = new BufferedReader(new FileReader(fileName));

			while ((sCurrentLine = br.readLine()) != null) {
				String[] splitted = sCurrentLine.split(" ");
				double x = Double.parseDouble(splitted[0]);
				double y = Double.parseDouble(splitted[1]);
				
				Point3D p = new Point3D(x, y, 0);
				Vertex v = new Vertex(p);
				result.add(v);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}
}
