package divideandconquer;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.CHService;
import geometry.Edge;
import geometry.Point3D;
import geometry.Vertex;
import tool.Smeshalist;
import util.Graham;
import util.Utils;

public class DivideAndConquer implements CHService {

	private static final TangentService tangentService = new TangentService();
	private static final Smeshalist tool = Smeshalist.getInstance();
	private static final Graham graham = new Graham();
	private static final List<Vertex> allPoints = new LinkedList<>();
	private static double yMax = -Double.MAX_VALUE;
	private static double yMin = Double.MAX_VALUE;
	private List<Double> medians = new LinkedList<>();
	private List<LinkedList<Vertex>> subCHs = new LinkedList<>();
	private int K;
	private static final int MAX_BREAKPOINTS = 10;
	private int breakpoints = 0;
	private long sleepTime = 1000;

	public DivideAndConquer(int k) {
		K = k;
	}

	@Override
	public LinkedList<Vertex> computeCH(LinkedList<Vertex> points) {
		allPoints.addAll(points);
		computeExtremalPoints();

		visualiseInitialSet();

		subCHs.addAll(computeCHInternal(points));
		visualiseSubCHs(subCHs, true);

		if (subCHs.size() > 1) {
			mergeSubCHs(subCHs);
		}

		visualiseSubCHs(subCHs, false);

		if (subCHs.size() == 1) {
			return subCHs.get(0);
		}
		return new LinkedList<>();
	}

	private void mergeSubCHs(List<LinkedList<Vertex>> subCHs2) {
		while (subCHs2.size() > 1) {
			int offset = 0; // quantity of made merges, use to determin offset

			for (; offset < subCHs2.size(); offset++) {
				LinkedList<Vertex> A = subCHs2.get(offset);
				LinkedList<Vertex> B = subCHs2.get(offset + 1);

				LinkedList<Vertex> merged = mergeTwoCH(A, B);

				subCHs2.remove(A);
				subCHs2.remove(B);
				subCHs2.add(offset, merged);
			}
		}
	}

	private LinkedList<Vertex> mergeTwoCH(LinkedList<Vertex> A, LinkedList<Vertex> B) {
		if (A.size() == 0) {
			return B;
		}
		if (B.size() == 0) {
			return A;
		}

		Vertex extreamA = A.get(0);
		for (Vertex point3d : A) {
			if (extreamA.getPoint().getX() < point3d.getPoint().getX()) {
				extreamA = point3d;
			}
		}

		Vertex extreamB = B.get(0);
		for (Vertex point3d : B) {
			if (extreamB.getPoint().getX() > point3d.getPoint().getX()) {
				extreamB = point3d;
			}
		}

		// finding upper tangent
		Vertex upperA = extreamA;
		Vertex upperB = extreamB;
		Vertex nextA = A.get((A.indexOf(upperA) + 1) % A.size());
		Vertex nextB = B.get(B.indexOf(upperB) > 0 ? B.indexOf(upperB) - 1 : B.size() - 1);

		while (!tangentService.isTangent(upperA, upperB, A, B, true)
				|| tangentService.isTangent(upperA, nextB, A, B, true)
				|| tangentService.isTangent(nextA, upperB, A, B, true)) {
			visualiseUpperTangent(upperA, upperB);
			while (!tangentService.isTangent(upperB, upperA, B, true)
					|| tangentService.isTangent(nextB, upperA, B, true)) {
				int index = B.indexOf(upperB) - 1; // upper neighbour
				if (index < 0) {
					index = B.size() - 1;
				}
				upperB = nextB;
				nextB = B.get(index);
				visualiseUpperTangent(upperA, upperB);
			}

			while (!tangentService.isTangent(upperA, upperB, A, false)
					|| tangentService.isTangent(nextA, upperB, A, false)) {
				int index = (A.indexOf(upperA) + 1) % A.size(); // upper
																// neighbour
				upperA = nextA;
				nextA = A.get(index);
				visualiseUpperTangent(upperA, upperB);
			}
		}

		// finding down tangent
		Vertex downA = extreamA;
		Vertex downB = extreamB;
		nextA = A.get(A.indexOf(downA) > 0 ? A.indexOf(downA) - 1 : A.size() - 1);
		nextB = B.get((B.indexOf(downB) + 1) % B.size());

		while (!tangentService.isTangent(downA, downB, A, B, false)
				|| tangentService.isTangent(downA, nextB, A, B, false)
				|| tangentService.isTangent(nextA, downB, A, B, false)) {
			visualiseBothTangent(downA, downB, upperA, upperB);
			while (!tangentService.isTangent(downB, downA, B, false)
					|| tangentService.isTangent(nextB, downA, B, false)) {
				int index = (B.indexOf(downB) + 1) % B.size();
				downB = nextB;
				nextB = B.get(index);
				visualiseBothTangent(downA, downB, upperA, upperB);
			}

			while (!tangentService.isTangent(downA, downB, A, true)
					|| tangentService.isTangent(nextA, downB, A, true)) {
				int index = A.indexOf(downA) - 1;
				if (index < 0) {
					index = A.size() - 1;
				}
				downA = nextA;
				nextA = A.get(index);
				visualiseBothTangent(downA, downB, upperA, upperB);
			}
		}

		LinkedList<Vertex> result = new LinkedList<>();

		// add left chain of A
		for (int i = A.indexOf(upperA);; i++) {
			if (i == A.size()) {
				i = 0;
			}

			result.add(A.get(i));

			if (A.get(i).equals(downA)) {
				break;
			}
		}

		// add right chain of B
		for (int i = B.indexOf(downB);; i++) {
			if (i == B.size()) {
				i = 0;
			}

			result.add(B.get(i));

			if (B.get(i).equals(upperB)) {
				break;
			}
		}

		return result;
	}

	private void visualiseBothTangent(Vertex downA, Vertex downB, Vertex upperA, Vertex upperB) {
		Edge e1 = Utils.pointsToEdge(upperA, upperB, Utils.ACTIVE_SEGMENT);
		tool.addGeometry(e1);
		Edge e2 = Utils.pointsToEdge(downA, downB, Utils.ACTIVE_SEGMENT);
		tool.addGeometry(e2);

		visualiseSubCHs(subCHs, false);
	}

	private void visualiseUpperTangent(Vertex upperA, Vertex upperB) {
		Edge e = Utils.pointsToEdge(upperA, upperB, Utils.ACTIVE_SEGMENT);
		tool.addGeometry(e);

		visualiseSubCHs(subCHs, false);
	}

	private List<LinkedList<Vertex>> computeCHInternal(LinkedList<Vertex> points) {
		List<LinkedList<Vertex>> result = new LinkedList<>();

		if (points.size() <= K) {
			result.add(graham.computeCH(points));
		} else {
			LinkedList<Vertex> A = new LinkedList<>();
			LinkedList<Vertex> B = new LinkedList<>();
			double median = getMedian(points);

			for (Vertex point3d : points) {
				if (point3d.getPoint().getX() <= median && A.size() < points.size() / 2) {
					A.add(point3d);
				} else {
					B.add(point3d);
				}
			}

			result.addAll(computeCHInternal(A));
			result.addAll(computeCHInternal(B));
		}

		return result;
	}

	private void computeExtremalPoints() {
		for (Vertex point3d : allPoints) {
			if (point3d.getPoint().getY() < yMin) {
				yMin = point3d.getPoint().getY();
			}

			if (point3d.getPoint().getY() > yMax) {
				yMax = point3d.getPoint().getY();
			}
		}
	}

	private double getMedian(List<Vertex> points) {
		points.sort(new Comparator<Vertex>() {

			@Override
			public int compare(Vertex o1, Vertex o2) {
				return (int) Math.signum(o1.getPoint().getX() - o2.getPoint().getX());
			}
		});

		double result;

		if (points.size() % 2 == 0) {
			int index = (int) points.size() / 2;
			result = (points.get(index - 1).getPoint().getX() + points.get(index).getPoint().getX()) / 2;
		} else {
			result = points.get(points.size() / 2).getPoint().getX();
		}

		medians.add(result);
		return result;
	}

	private void visualiseInitialSet() {
		for (Vertex point3d : allPoints) {
			point3d.setGroupId(Utils.OTHERS);
		}
		allPoints.forEach(tool::addGeometry);
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

	private void visualiseSubCHs(List<LinkedList<Vertex>> subCHs2, boolean drawMedians) {

		allPoints.forEach(point -> point.setGroupId(Utils.OTHERS));

		int currGroupID = Utils.FIRST_GROUP;

		for (List<Vertex> CH : subCHs2) {
			for (Vertex point3d : CH) {
				point3d.setGroupId(currGroupID);
			}
			if (currGroupID == Utils.FIRST_GROUP) {
				currGroupID = Utils.SECOND_GROUP;
			} else {
				currGroupID = Utils.FIRST_GROUP;
			}
		}

		allPoints.forEach(tool::addGeometry);

		if (drawMedians) {
			for (Double median : medians) {
				Edge e = new Edge(new Point3D(median, yMin, 0), new Point3D(median, yMax, 0));
				e.setGroupId(Utils.CURRENT_CH);
				tool.addGeometry(e);
			}

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

		// visualise subSHs
		for (List<Vertex> CH : subCHs2) {
			for (int i = 0; i < CH.size(); i++) {
				Edge e = Utils.pointsToEdge(CH.get(i), CH.get((i + 1) % CH.size()), Utils.CURRENT_CH);
				tool.addGeometry(e);
			}
		}

		allPoints.forEach(tool::addGeometry);

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
