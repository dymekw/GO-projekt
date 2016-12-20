package util;

import geometry.Point3D;
import geometry.Vertex;

public class DeterminantCalc {
	private static final double EPSILON = 0.000001;
	
	public int getDeterminantSign(Point3D a, Point3D b, Point3D c) {
		double determinant = getDeterminant(a, b, c);
		
		if (determinant < -EPSILON) {
			return -1;
		} else if (determinant > EPSILON) {
			return 1;
		}
		
		return 0;
	}
	
	public double getDeterminant(Point3D a, Point3D b, Point3D c) {
		return 	a.getX()*b.getY() +
				a.getY()*c.getX() +
				b.getX()*c.getY() -
				b.getY()*c.getX() -
				c.getY()*a.getX() -
				a.getY()*b.getX();
	}

	public int getDeterminantSign(Vertex p, Vertex vertex, Vertex vertex2) {
		return getDeterminantSign(p.getPoint(), vertex.getPoint(), vertex2.getPoint());
	}
}
