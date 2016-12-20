package chan;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import app.CHService;
import geometry.Point3D;
import geometry.Vertex;
import util.DeterminantCalc;
import util.Graham;
import util.Utils;

public class Chan implements CHService {

	private int m;
	private int genVersion;
	private final Graham graham = new Graham();
	private final DeterminantCalc calc = new DeterminantCalc();

	private long totalTime = 0;
	private long subCHsComputationTime = 0;
	private long sortingTime = 0;

	public Chan(int m, int genVersion) {
		this.m = m;
		this.genVersion = genVersion;
	}

	@Override
	public LinkedList<Vertex> computeCH(LinkedList<Vertex> points) {
		List<Vertex> copy = new LinkedList<>(points);

		List<LinkedList<Vertex>> subSets = null;

		switch (genVersion) {
		case 1:
			subSets = generateSubSets(copy);
			break;
		default:
			subSets = generateSubSets2(copy);
		}
		long startTotal = System.nanoTime();

		long subCHsGenTime = System.nanoTime();
		List<LinkedList<Vertex>> subCHs = subSets.stream().map(graham::computeCH).collect(Collectors.toList());
		subCHsComputationTime = System.nanoTime() - subCHsGenTime;

		for (int i=0; i<subCHs.size(); i++) {
			LinkedList<Vertex> subCH = subCHs.get(i);
			for (Vertex vertex : subCH) {
				vertex.setGroupId(i);
			}
		}

		copy = new LinkedList<>(points);
		Vertex current = Utils.getInitialPoint(copy, true);
		LinkedList<Vertex> result = new LinkedList<>();
		result.add(current);

		for (int i = 0; i < m; i++) {
			long sortStart = System.nanoTime();
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
			util.PointComparator deterPointComparator = new util.PointComparator(current);
			current = Collections.min(candidates, deterPointComparator);
			sortingTime += System.nanoTime() - sortStart;


			if (current.equals(result.getFirst())) {
				break;
			} else {
				result.add(current);
			}

			if (i + 1 == m) {
				throw new RuntimeException("m is too small");
			}
		}

		totalTime = System.nanoTime() - startTotal;
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

	private List<LinkedList<Vertex>> generateSubSets(List<Vertex> copy) {
		List<LinkedList<Vertex>> result = new LinkedList<>();

		while (!copy.isEmpty()) {
			int quantity = Math.min(m, copy.size());

			LinkedList<Vertex> subSet = new LinkedList<>();

			for (int i = 0; i < quantity; i++) {
				Vertex p = copy.get(0);
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
			PointComparator comparator = new PointComparator(copy.get(0));
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

		public PointComparator(Vertex p) {
			this.p = p.getPoint();
		}

		@Override
		public int compare(Vertex o1, Vertex o2) {
			return (int) Math.signum(Utils.getDistance(o1.getPoint(), p) - Utils.getDistance(o2.getPoint(), p));
		}

	}

	@Override
	public Map<String, Long> getTimes() {
		Map<String, Long> result = new HashMap<>();

		result.put("Total time: ", totalTime);
		result.put("SubCHs computing time: ", subCHsComputationTime);
		result.put("Sort time: ", sortingTime);

		return result;
	}
}
