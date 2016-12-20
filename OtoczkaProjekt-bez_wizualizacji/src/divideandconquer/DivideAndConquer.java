package divideandconquer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import app.CHService;
import geometry.Vertex;
import util.Graham;

public class DivideAndConquer implements CHService {

	private static final TangentService tangentService = new TangentService();
	private static final Graham graham = new Graham();
	private List<LinkedList<Vertex>> subCHs = new LinkedList<>();
	private int K;

	public long totalTime = 0;
	public long dividingTime = 0;
	public long mergingTime = 0;

	public DivideAndConquer(int k) {
		K = k;
	}

	@Override
	public LinkedList<Vertex> computeCH(LinkedList<Vertex> points) {
		long startTotal = System.nanoTime();

		long startDividing = System.nanoTime();
		subCHs.addAll(computeCHInternal(points));
		dividingTime = System.nanoTime() - startDividing;

		if (subCHs.size() > 1) {
			long startMerging = System.nanoTime();
			mergeSubCHs(subCHs);
			mergingTime = System.nanoTime() - startMerging;
		}

		totalTime = System.nanoTime() - startTotal;
		return subCHs.get(0);
	}

	private void mergeSubCHs(List<LinkedList<Vertex>> subCHs2) {
		while (subCHs2.size() > 1) {
			int offset = 0;

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

		Vertex upperA = extreamA;
		Vertex upperB = extreamB;
		Vertex nextA = A.get((A.indexOf(upperA) + 1) % A.size());
		Vertex nextB = B.get(B.indexOf(upperB) > 0 ? B.indexOf(upperB) - 1 : B.size() - 1);

		while (!tangentService.isTangent(upperA, upperB, A, B, true)
				|| tangentService.isTangent(upperA, nextB, A, B, true)
				|| tangentService.isTangent(nextA, upperB, A, B, true)) {
			while (!tangentService.isTangent(upperB, upperA, B, true)
					|| tangentService.isTangent(nextB, upperA, B, true)) {
				int index = B.indexOf(upperB) - 1;
				if (index < 0) {
					index = B.size() - 1;
				}
				upperB = nextB;
				nextB = B.get(index);
			}

			while (!tangentService.isTangent(upperA, upperB, A, false)
					|| tangentService.isTangent(nextA, upperB, A, false)) {
				int index = (A.indexOf(upperA) + 1) % A.size();

				upperA = nextA;
				nextA = A.get(index);
			}
		}

		Vertex downA = extreamA;
		Vertex downB = extreamB;
		nextA = A.get(A.indexOf(downA) > 0 ? A.indexOf(downA) - 1 : A.size() - 1);
		nextB = B.get((B.indexOf(downB) + 1) % B.size());

		while (!tangentService.isTangent(downA, downB, A, B, false)
				|| tangentService.isTangent(downA, nextB, A, B, false)
				|| tangentService.isTangent(nextA, downB, A, B, false)) {
			while (!tangentService.isTangent(downB, downA, B, false)
					|| tangentService.isTangent(nextB, downA, B, false)) {
				int index = (B.indexOf(downB) + 1) % B.size();
				downB = nextB;
				nextB = B.get(index);
			}

			while (!tangentService.isTangent(downA, downB, A, true)
					|| tangentService.isTangent(nextA, downB, A, true)) {
				int index = A.indexOf(downA) - 1;
				if (index < 0) {
					index = A.size() - 1;
				}
				downA = nextA;
				nextA = A.get(index);
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

		return result;
	}

	@Override
	public Map<String, Long> getTimes() {
		Map<String, Long> result = new HashMap<>();

		result.put("Total time: ", totalTime);
		result.put("SubCHs computing time: ", dividingTime);
		result.put("Merging time: ", mergingTime);

		return result;
	}

}
