package util;

import java.util.List;

import geometry.Edge;
import geometry.Point3D;
import geometry.Vertex;

public class Utils {
	public static final int FIRST_GROUP = 7;	//QuickHull only
	public static final int SECOND_GROUP = 6;	//QuickHull only
	public static final int INITIAL = 5;
	public static final int OTHERS = 4;
	public static final int REMOVED = 3;
	public static final int POINT_CH = 2;
	public static final int ACTIVE_SEGMENT = 1;
	public static final int CURRENT_CH = 0;
	
	public static double getDistance(Point3D a, Point3D b) {
		double result = (a.getX() - b.getX()) * (a.getX() - b.getX());
		result += (a.getY() - b.getY()) * (a.getY() - b.getY());
		result += (a.getZ() - b.getZ()) * (a.getZ() - b.getZ());
		
		return Math.sqrt(result);
	}
	
	public static Vertex getInitialPoint(List<Vertex> points, boolean removeInitial) {
		if (points.isEmpty()) {
			return null;
		}
		
		Vertex result = points.stream().findAny().get();
		
		for (Vertex point3d : points) {
			if (point3d.getPoint().getY() < result.getPoint().getY()) {
				result = point3d;
			} else if (point3d.getPoint().getY() == result.getPoint().getY()) {
				if (point3d.getPoint().getX() < result.getPoint().getX()) {
					result = point3d;
				}
			}
		}
		
		if (removeInitial) {
			points.remove(result);
		}
		
		return result;
	}
	
	public static Edge pointsToEdge(Point3D a, Point3D b, int group) {
		Edge e = new Edge(a,b);
		e.setGroupId(group);
		return e;
	}
}
