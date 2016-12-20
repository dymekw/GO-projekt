package quickhull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import app.CHService;
import geometry.Edge;
import geometry.Vertex;
import tool.Smeshalist;
import util.DeterminantCalc;
import util.Utils;

public class QuickHull implements CHService {

	private static final DeterminantCalc calc = new DeterminantCalc();
	private static final Smeshalist tool = Smeshalist.getInstance();
	private List<Vertex> allPoints;
	private LinkedList<Vertex> currentCH = new LinkedList<>();
	private static final int MAX_BREAKPOINTS = 10;
	private int breakpoints = 0;
	private long sleepTime = 3000;

	@Override
	public LinkedList<Vertex> computeCH(LinkedList<Vertex> points) {
		allPoints = points;
		Map<String, Vertex> initialPoints = getInitialPoints(points);
		Vertex a = initialPoints.get("a");
		Vertex b = initialPoints.get("b");

		visualiseInitialPointsFound(points, a, b);

		List<Vertex> A = new LinkedList<>();
		List<Vertex> B = new LinkedList<>();

		for (Vertex point3d : points) {
			int sign = calc.getDeterminantSign(a, b, point3d);

			if (sign == 1) {
				A.add(point3d);
			} else if (sign == -1) {
				B.add(point3d);
			}
		}

		visualiseFirstSetDivision(A, B, a, b, points);

		currentCH.add(a);
		computeCH(a, b, A);
		currentCH.add(b);
		computeCH(b, a, B);

		visualiseResult(points, currentCH, true, true, true);
		
		return currentCH;
	}

	private List<Vertex> computeCH(Vertex a, Vertex b, List<Vertex> points) {
		if (points.isEmpty()) {
			return new LinkedList<>();
		}

		Vertex c = getFarthestPoint(a, b, points);
		
		visualiseResult(allPoints, currentCH, false, false, false);
		visualiseFarthestPointFound(a, b, c, points);

		List<Vertex> A = new LinkedList<>();
		List<Vertex> B = new LinkedList<>();

		for (Vertex point3d : points) {
			int signA = calc.getDeterminantSign(a, c, point3d);

			if (signA == 1) {
				A.add(point3d);
			} else if (signA == -1) {
				int signB = calc.getDeterminantSign(c, b, point3d);
				if (signB == 1) {
					B.add(point3d);
				}
			}
		}
		
		visualiseResult(allPoints, currentCH, false, false, false);
		visualiseSetDivision(a, b, c, A, B, points);

		List<Vertex> result = new LinkedList<>();
		result.addAll(computeCH(a, c, A));
		result.add(c);
		currentCH.add(c);
		result.addAll(computeCH(c, b, B));
		return result;
	}

	private Vertex getFarthestPoint(Vertex a, Vertex b, List<Vertex> points) {
		Vertex result = null;
		double maxArea = 0;

		for (Vertex point3d : points) {
			double area = Math.abs((a.getPoint().getX() - point3d.getPoint().getX()) * (b.getPoint().getY() - a.getPoint().getY())
					- (a.getPoint().getX() - b.getPoint().getX()) * (point3d.getPoint().getY() - a.getPoint().getY())) / 2;

			if (maxArea < area) {
				maxArea = area;
				result = point3d;
			}
		}

		return result;
	}

	private Map<String, Vertex> getInitialPoints(List<Vertex> points) {
		Map<String, Vertex> result = new HashMap<>();

		if (points.isEmpty()) {
			return result;
		}

		Vertex minX = points.get(0);
		Vertex maxX = points.get(0);

		for (Vertex point3d : points) {
			if (point3d.getPoint().getX() < minX.getPoint().getX()) {
				minX = point3d;
			} else if (point3d.getPoint().getX() == minX.getPoint().getX()) {
				if (point3d.getPoint().getY() < minX.getPoint().getY()) {
					minX = point3d;
				}
			}

			if (point3d.getPoint().getX() > maxX.getPoint().getX()) {
				maxX = point3d;
			} else if (point3d.getPoint().getX() == maxX.getPoint().getX()) {
				if (point3d.getPoint().getY() > maxX.getPoint().getY()) {
					maxX = point3d;
				}
			}
		}

		result.put("a", minX);
		result.put("b", maxX);

		return result;
	}

	private void visualiseInitialPointsFound(List<Vertex> points, Vertex a, Vertex b) {
		for (Vertex point3d : points) {
			point3d.setGroupId(Utils.OTHERS);
		}
		a.setGroupId(Utils.INITIAL);
		b.setGroupId(Utils.INITIAL);

		Edge e = Utils.pointsToEdge(a, b, Utils.ACTIVE_SEGMENT);

		for (Vertex point3d : points) {
			tool.addGeometry(point3d);
		}
		tool.addGeometry(e);
		tool.flushBuffer();
		tool.clean();
		tool.render();
		tool.breakpoint();
	}

	private void visualiseFirstSetDivision(List<Vertex> A, List<Vertex> B, Vertex a, Vertex b,
			List<Vertex> points) {
		points.forEach(point -> point.setGroupId(Utils.OTHERS));
		A.forEach(point -> point.setGroupId(Utils.FIRST_GROUP));
		B.forEach(point -> point.setGroupId(Utils.SECOND_GROUP));
		
		a.setGroupId(Utils.INITIAL);
		b.setGroupId(Utils.INITIAL);
		
		Edge e = Utils.pointsToEdge(a, b, Utils.ACTIVE_SEGMENT);
		
		points.forEach(tool::addGeometry);
		tool.addGeometry(e);
		
		tool.flushBuffer();
		tool.clean();
		tool.render();
		tool.breakpoint();
	}

	private void visualiseResult(List<Vertex> points, List<Vertex> result, boolean isClosedChain, boolean withPoints, boolean sendData) {
		if (withPoints) {
			points.forEach(point -> point.setGroupId(Utils.OTHERS));
		}
		
		for (int i=0; i<result.size(); i++) {
			Vertex a = result.get(i);
			Vertex b =null;
			if (isClosedChain) {
				b = result.get((i+1)%result.size());
			} else {
				if (i == result.size()-1) {
					break;
				} else {
					b = result.get(i+1);
				}
			}
			
			Edge e = Utils.pointsToEdge(a, b, Utils.CURRENT_CH);
			a.setGroupId(Utils.POINT_CH);
			b.setGroupId(Utils.POINT_CH);
			
			tool.addGeometry(e);
		}
		
		if (withPoints) {
			points.forEach(tool::addGeometry);
		}
		if (sendData) {
			tool.flushBuffer();
			tool.clean();
			tool.render();
		}
	}
	
	private void visualiseFarthestPointFound(Vertex a, Vertex b, Vertex c, List<Vertex> points) {
		allPoints.forEach(point -> point.setGroupId(Utils.OTHERS));
		points.forEach(point -> point.setGroupId(Utils.FIRST_GROUP));
		
		Edge e = Utils.pointsToEdge(a, b, Utils.ACTIVE_SEGMENT);
		a.setGroupId(Utils.POINT_CH);
		b.setGroupId(Utils.POINT_CH);
		c.setGroupId(Utils.INITIAL);
		
		allPoints.forEach(tool::addGeometry);
		tool.addGeometry(e);
		tool.flushBuffer();
		tool.clean();
		tool.render();
		
		if (breakpoints < MAX_BREAKPOINTS) {
			tool.breakpoint();
			breakpoints++;
		} else {
			try {
				TimeUnit.MILLISECONDS.sleep(sleepTime);
				sleepTime *= 0.9;
				
				sleepTime = Math.max(sleepTime, 150);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private void visualiseSetDivision(Vertex a, Vertex b, Vertex c, List<Vertex> A, List<Vertex> B,
			List<Vertex> points) {
		allPoints.forEach(point -> point.setGroupId(Utils.OTHERS));
		points.forEach(point -> point.setGroupId(Utils.REMOVED));
		A.forEach(point -> point.setGroupId(Utils.FIRST_GROUP));
		B.forEach(point -> point.setGroupId(Utils.SECOND_GROUP));
		
		a.setGroupId(Utils.POINT_CH);
		b.setGroupId(Utils.POINT_CH);
		c.setGroupId(Utils.POINT_CH);
		
		Edge e1 = Utils.pointsToEdge(a, b, Utils.ACTIVE_SEGMENT);
		Edge e2 = Utils.pointsToEdge(a, c, Utils.ACTIVE_SEGMENT);
		Edge e3 = Utils.pointsToEdge(c, b, Utils.ACTIVE_SEGMENT);
		
		allPoints.forEach(tool::addGeometry);
		tool.addGeometry(e1);
		tool.addGeometry(e2);
		tool.addGeometry(e3);
		
		tool.flushBuffer();
		tool.clean();
		tool.render();
		
		
		if (breakpoints < MAX_BREAKPOINTS) {
			tool.breakpoint();
			breakpoints++;
		} else {
			try {
				TimeUnit.MILLISECONDS.sleep(sleepTime);
				sleepTime *= 0.9;
				
				sleepTime = Math.max(sleepTime, 150);
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
	}
}
