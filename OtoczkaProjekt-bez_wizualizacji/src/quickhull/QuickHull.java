package quickhull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import app.CHService;
import geometry.Vertex;
import util.DeterminantCalc;

public class QuickHull implements CHService {

	private static final DeterminantCalc calc = new DeterminantCalc();
	private LinkedList<Vertex> currentCH = new LinkedList<>();

	private long totalTime = 0;
	private long farthestComTime = 0;
	private long dividingTime = 0;

	@Override
	public LinkedList<Vertex> computeCH(LinkedList<Vertex> points) {
		long startTotal = System.nanoTime();
		Map<String, Vertex> initialPoints = getInitialPoints(points);
		Vertex a = initialPoints.get("a");
		Vertex b = initialPoints.get("b");

		List<Vertex> A = new LinkedList<>();
		List<Vertex> B = new LinkedList<>();

		long dividingStart = System.nanoTime();
		for (Vertex point3d : points) {
			int sign = calc.getDeterminantSign(a.getPoint(), b.getPoint(), point3d.getPoint());

			if (sign == 1) {
				A.add(point3d);
			} else if (sign == -1) {
				B.add(point3d);
			}
		}
		dividingTime += System.nanoTime() - dividingStart;

		currentCH.add(a);
		computeCH(a, b, A);
		currentCH.add(b);
		computeCH(b, a, B);

		totalTime = System.nanoTime() - startTotal;
		return currentCH;
	}

	private List<Vertex> computeCH(Vertex a, Vertex b, List<Vertex> points) {
		if (points.isEmpty()) {
			return new LinkedList<>();
		}

		Vertex c = getFarthestPoint(a, b, points);

		List<Vertex> A = new LinkedList<>();
		List<Vertex> B = new LinkedList<>();

		long dividingStart = System.nanoTime();
		for (Vertex point3d : points) {
			int signA = calc.getDeterminantSign(a.getPoint(), c.getPoint(), point3d.getPoint());

			if (signA == 1) {
				A.add(point3d);
			} else if (signA == -1) {
				int signB = calc.getDeterminantSign(c.getPoint(), b.getPoint(), point3d.getPoint());
				if (signB == 1) {
					B.add(point3d);
				}
			}
		}
		dividingTime += System.nanoTime() - dividingStart;

		List<Vertex> result = new LinkedList<>();
		result.addAll(computeCH(a, c, A));
		result.add(c);
		currentCH.add(c);
		result.addAll(computeCH(c, b, B));
		return result;
	}

	private Vertex getFarthestPoint(Vertex a, Vertex b, List<Vertex> points) {
		long start = System.nanoTime();
		Vertex result = null;
		double maxArea = 0;

		for (Vertex point3d : points) {
			double area = Math.abs((a.getPoint().getX() - point3d.getPoint().getX())
					* (b.getPoint().getY() - a.getPoint().getY())
					- (a.getPoint().getX() - b.getPoint().getX()) * (point3d.getPoint().getY() - a.getPoint().getY()))
					/ 2;

			if (maxArea < area) {
				maxArea = area;
				result = point3d;
			}
		}
		farthestComTime += System.nanoTime() - start;
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

	@Override
	public Map<String, Long> getTimes() {
		Map<String, Long> result = new HashMap<>();

		result.put("Total time: ", totalTime);
		result.put("Farthest point computation time: ", farthestComTime);
		result.put("Total dividing into subsets time: ", dividingTime);

		return result;
	}

}
