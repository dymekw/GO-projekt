package graham;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import app.CHService;
import geometry.Vertex;
import util.DeterminantCalc;
import util.PointComparator;
import util.Utils;


public class Graham implements CHService{
	
	private static final DeterminantCalc calc = new DeterminantCalc();
	
	private long totalTime = 0;
	private long erasingTime = 0;
	private long sortingTime = 0;

	@Override
	public LinkedList<Vertex> computeCH(LinkedList<Vertex> points) {
		long startTotal = System.nanoTime();
		Vertex initial = Utils.getInitialPoint(points, true);
		initial.setGroupId(Utils.INITIAL);
		
		long startSorting = System.nanoTime();
		PointComparator comparator = new PointComparator(initial);
		points.sort(comparator);
		sortingTime = System.nanoTime() - startSorting;
		
		long startErasing = System.nanoTime();
		deleteCollinearPoints(points, initial);
		erasingTime = System.nanoTime() - startErasing;

		Stack<Vertex> stack = initStack(points, initial);
		
		for (int i=2; i<points.size(); i++) {
			Vertex t1 = stack.get(stack.size()-2);
			Vertex t = stack.get(stack.size()-1);
			
			if (calc.getDeterminantSign(t1.getPoint(), t.getPoint(), points.get(i).getPoint()) > 0) {
				stack.push(points.get(i));
			} else {
				stack.pop();
				i--;
			}
		}
		totalTime = System.nanoTime() - startTotal;
		return new LinkedList<>(stack);
	}
	
	private Stack<Vertex> initStack(List<Vertex> points, Vertex initial) {
		Stack<Vertex> stack = new Stack<>();
		stack.push(initial);
		stack.push(points.get(0));
		stack.push(points.get(1));
		
		return stack;
	}

	private void deleteCollinearPoints(List<Vertex> points, Vertex initial) {
		for (int i=0; i<points.size()-1; i++) {
			Vertex a = points.get(i);
			Vertex b = points.get(i+1);
			
			if (calc.getDeterminantSign(a.getPoint(), initial.getPoint(), b.getPoint()) == 0) {
				points.remove(b);
				i--;
			}
		}
	}

	@Override
	public Map<String, Long> getTimes() {
		Map<String, Long> result = new HashMap<>();
		
		result.put("Total time: ", totalTime);
		result.put("Deleting collinear: ", erasingTime);
		result.put("Sort time: ", sortingTime);
		
		return result;
	}
}
