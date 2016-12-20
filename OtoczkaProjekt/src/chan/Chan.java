package chan;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import app.CHService;
import geometry.Edge;
import geometry.Point3D;
import geometry.Vertex;
import tool.Smeshalist;
import util.DeterminantCalc;
import util.Graham;
import util.Utils;

public class Chan implements CHService {

	private int m;
	private int genVersion;
	private final Graham graham = new Graham();
	private final Random r = new Random();
	private static final Smeshalist tool = Smeshalist.getInstance();
	private final DeterminantCalc calc = new DeterminantCalc();

	public Chan(int m, int genVersion) {
		this.m = m;
		this.genVersion = genVersion;
	}

	@Override
	public LinkedList<Vertex> computeCH(LinkedList<Vertex> points) {
		List<Vertex> copy = new LinkedList<>(points);
		
		visualisePoints(points);

		List<LinkedList<Vertex>> subSets = null;

		switch (genVersion) {
		case 1:
			subSets = generateSubSets(copy);
			break;
		default:
			subSets = generateSubSets2(copy);
		}

		List<LinkedList<Vertex>> subCHs = subSets.stream().map(graham::computeCH).collect(Collectors.toList());
		
		visualiseGeneratedSubCHs(points, subCHs);
		
		copy = new LinkedList<>(points);
		Vertex current = Utils.getInitialPoint(copy, true);
		LinkedList<Vertex> result = new LinkedList<>();
		result.add(current);
		
		for (int i=0; i<m; i++) {
			util.PointComparator deterPointComparator = new util.PointComparator(current);
			LinkedList<Vertex> candidates = new LinkedList<>();

			for (LinkedList<Vertex> list : subCHs) {
				if (list.getLast().getGroupId() == current.getGroupId()) {
					candidates.add(list.get((list.indexOf(current)+1) % list.size()));
				} else {
					Vertex candidate = getCHCandidate(list, current, 0, list.size()-1);
					if (Objects.nonNull(candidate)) {
						candidates.add(candidate);
					}
				}
			}
			candidates.sort(deterPointComparator);
			
			visualiseCandidates(points, subCHs, candidates, result, current);
			
			current = candidates.getFirst();
			
			if (current.equals(result.getFirst())) {
				break;
			} else {
				result.add(current);
			}
			
			if (i+1 == m) {
				throw new RuntimeException("m is too small");
			}
		}
		
		visualiseResult(points, result);
		
		return result;
	}

	private Vertex getCHCandidate(LinkedList<Vertex> CH, Vertex P, int first, int last) {
		int firstPrevSign = calc.getDeterminantSign(P, CH.get(first), CH.get(first > 0 ? first-1 : CH.size()-1));
		int firstNextSign = calc.getDeterminantSign(P, CH.get(first), CH.get((CH.size()+1) % CH.size()));
		
		while (first < last) {
			int c = (first + last)/2;
			
			int cPrev = calc.getDeterminantSign(P, CH.get(c), CH.get(c > 0 ? c-1 : CH.size()-1));
			int cNext = calc.getDeterminantSign(P, CH.get(c), CH.get((c+1)%CH.size()));
			int cSide = calc.getDeterminantSign(P, CH.get(first), CH.get(c));
			
			if (cPrev != -1 && cNext != -1) {
				return CH.get(c);
			} else if (cSide == 1 && (firstNextSign == -1 || firstNextSign == firstPrevSign) || cSide == -1 && cPrev == -1) {
				last = c;
			} else {
				first = c+1;
				firstPrevSign = -cNext;
				firstNextSign = calc.getDeterminantSign(P, CH.get(first), CH.get((first+1)%CH.size()));
			}
		}
		
		
		return CH.get(first);
	}

	private void visualiseCandidates(List<Vertex> points, List<LinkedList<Vertex>> subCHs, LinkedList<Vertex> candidates, List<Vertex> result, Vertex initial) {
		for (Vertex point3d : candidates) {
			Edge e = Utils.pointsToEdge(initial, point3d, Utils.ACTIVE_SEGMENT);
			tool.addGeometry(e);
		}
		
		for (int i = 0; i < subCHs.size(); i++) {
			for (int j = 0; j < subCHs.get(i).size(); j++) {
				Vertex a = subCHs.get(i).get(j);
				Vertex b = subCHs.get(i).get((j + 1) % subCHs.get(i).size());
				Edge e = Utils.pointsToEdge(a, b, 10+i);
				tool.addGeometry(e);
			}
		}
		
		points.forEach(tool::addGeometry);
		
		for (int i=0; i<result.size()-1; i++) {
			Vertex a = result.get(i);
			Vertex b = result.get(i + 1);
			
			Edge e = Utils.pointsToEdge(a, b, Utils.CURRENT_CH);
			tool.addGeometry(e);
		}
		
		tool.flushBuffer();
		tool.clean();
		tool.render();
		tool.breakpoint();
	}

	private void visualiseResult(List<Vertex> points, LinkedList<Vertex> result) {
		for (Vertex point3d : points) {
			point3d.setGroupId(Utils.OTHERS);
		}
		
		for (int j = 0; j < result.size(); j++) {
			Vertex a = result.get(j);
			Vertex b = result.get((j + 1) % result.size());
			Edge e = Utils.pointsToEdge(a, b, Utils.CURRENT_CH);
			result.get(j).setGroupId(Utils.POINT_CH);
			tool.addGeometry(e);
		}
		
		points.forEach(tool::addGeometry);
		
		tool.flushBuffer();
		tool.clean();
		tool.render();
	}

	private void visualiseGeneratedSubCHs(List<Vertex> points, List<LinkedList<Vertex>> subCHs) {
		for (Vertex point3d : points) {
			point3d.setGroupId(Utils.OTHERS);
		}
		for (int i = 0; i < subCHs.size(); i++) {
			for (int j = 0; j < subCHs.get(i).size(); j++) {
				Vertex a = subCHs.get(i).get(j);
				Vertex b = subCHs.get(i).get((j + 1) % subCHs.get(i).size());
				Edge e = Utils.pointsToEdge(a, b, 10+i);
				subCHs.get(i).get(j).setGroupId(10+i);
				tool.addGeometry(e);
			}
		}
		for (Vertex point3d : points) {
			tool.addGeometry(point3d);
		}
		tool.flushBuffer();
		tool.clean();
		tool.render();
		tool.breakpoint();
	}

	private void visualisePoints(List<Vertex> points) {
		for (Vertex point3d : points) {
			point3d.setGroupId(Utils.OTHERS);
			tool.addGeometry(point3d);
		}
		
		tool.flushBuffer();
		tool.clean();
		tool.render();
		tool.breakpoint();
	}

	private List<LinkedList<Vertex>> generateSubSets(List<Vertex> copy) {
		List<LinkedList<Vertex>> result = new LinkedList<>();

		while (!copy.isEmpty()) {
			int quantity = Math.min(m, copy.size());

			LinkedList<Vertex> subSet = new LinkedList<>();

			for (int i = 0; i < quantity; i++) {
				Vertex p = copy.get(r.nextInt(copy.size()));
				subSet.add(p);
				copy.remove(p);
			}

			result.add(subSet);
		}

		return result;
	}

	private List<LinkedList<Vertex>> generateSubSets2(List<Vertex> copy) {
		List<LinkedList<Vertex>> result = new LinkedList<>();
		Comparator<Vertex> xComp = new Comparator<Vertex>() {
			@Override
			public int compare(Vertex o1, Vertex o2) {
				return (int) Math.signum(o1.getPoint().getX() - o2.getPoint().getX());
			}
		};
		

		while (!copy.isEmpty()) {
			int quantity = Math.min(m, copy.size());

			LinkedList<Vertex> subSet = new LinkedList<>();

			copy.sort(xComp);
			PointComparator comparator = new PointComparator(copy.get(0).getPoint());
			copy.sort(comparator);
			for (int i = 0; i < quantity; i++) {
				Vertex p = copy.get(i);
				subSet.add(p);
			}

			copy.removeAll(subSet);
			result.add(subSet);
		}

		return result;
	}

	private static class PointComparator implements Comparator<Vertex> {

		private Point3D p;

		public PointComparator(Point3D p) {
			this.p = p;
		}

		@Override
		public int compare(Vertex o1, Vertex o2) {
			return (int) Math.signum(Utils.getDistance(o1.getPoint(), p) - Utils.getDistance(o2.getPoint(), p));
		}

	}
}
