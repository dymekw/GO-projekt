package jarvis;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import app.CHService;
import geometry.Vertex;
import util.PointComparator;
import util.Utils;

public class Jarvis implements CHService {
	
	private long totalTime = 0;
	private long sortingTime = 0;

	@Override
	public LinkedList<Vertex> computeCH(LinkedList<Vertex> points) {
		long startTotal = System.nanoTime();
		Vertex initial = Utils.getInitialPoint(points, false);
		LinkedList<Vertex> list = new LinkedList<>();
		
		Vertex current = initial;

		do {
			list.add(current);
			
			long sortStart = System.nanoTime();
			PointComparator comparator = new PointComparator(current);
			points.sort(comparator);
			sortingTime += System.nanoTime() - sortStart;
			
			current = points.get(0);			
		} while (current != initial);
		
		totalTime = System.nanoTime() - startTotal;
		return list;
	}

	@Override
	public Map<String, Long> getTimes() {
		Map<String, Long> result = new HashMap<>();
		
		result.put("Total time: ", totalTime);
		result.put("Sort time: ", sortingTime);
		
		return result;
	}
}
