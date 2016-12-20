package util;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import geometry.Point3D;
import geometry.Vertex;

public class Graham {
	private static final DeterminantCalc calc = new DeterminantCalc();

	public LinkedList<Vertex> computeCH(LinkedList<Vertex> points) {
		if (points.size() <= 2) {
			return points;
		}
		Vertex initial = Utils.getInitialPoint(points, true);
		
		PointComparator comparator = new PointComparator(initial);
		points.sort(comparator);
		
		deleteCollinearPoints(points, initial.getPoint());
		
		if (points.size() <= 2) {
			return points;
		}

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

		return new LinkedList<>(stack);
	}
	
	private Stack<Vertex> initStack(List<Vertex> points, Vertex initial) {
		Stack<Vertex> stack = new Stack<>();
		stack.push(initial);
		stack.push(points.get(0));
		stack.push(points.get(1));
		
		return stack;
	}

	private void deleteCollinearPoints(List<Vertex> points, Point3D initial) {
		for (int i=0; i<points.size()-1; i++) {
			Point3D a = points.get(i).getPoint();
			Point3D b = points.get(i+1).getPoint();
			
			if (calc.getDeterminantSign(a, initial, b) == 0) {
				points.remove(b);
				i--;
			}
		}
	}
}
