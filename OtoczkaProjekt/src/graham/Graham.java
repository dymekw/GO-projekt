package graham;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import app.CHService;
import geometry.Vertex;
import tool.Smeshalist;
import util.DeterminantCalc;
import util.PointComparator;
import util.Utils;


public class Graham implements CHService{
	
	private static final DeterminantCalc calc = new DeterminantCalc();
	private static final Smeshalist tool = Smeshalist.getInstance();
	private Collection<Vertex> removed = new LinkedList<>();

	@Override
	public LinkedList<Vertex> computeCH(LinkedList<Vertex> points) {
		int sleepTime = 5000;
		
		Vertex initial = Utils.getInitialPoint(points, true);
		initial.setGroupId(Utils.INITIAL);
		
		PointComparator comparator = new PointComparator(initial);
		points.sort(comparator);
		
		deleteCollinearPoints(points, initial);
		
		for (Vertex point3d : points) {
			tool.addGeometry(point3d);
		}
		tool.flushBuffer();
		tool.render();

		Stack<Vertex> stack = initStack(points, initial);
		
		for (int i=2; i<points.size(); i++) {
			Vertex t1 = stack.get(stack.size()-2);
			Vertex t = stack.get(stack.size()-1);
			
			tool.addGeometry(initial);
			for (Vertex point3d : points) {
				if (!stack.contains(point3d)) {
					point3d.setGroupId(Utils.OTHERS);
					tool.addGeometry(point3d);
				}
			}
			tool.addGeometry(Utils.pointsToEdge(t, points.get(i), Utils.ACTIVE_SEGMENT));
			visualiseStack(stack, false);
			tool.flushBuffer();
			tool.clean();
			tool.render();
			
			if (i==2) {
				tool.breakpoint();
			} else{
				try {
					TimeUnit.MILLISECONDS.sleep(sleepTime);
					sleepTime *= 0.7;
					
					sleepTime = Math.max(sleepTime, 150);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			//
			
			if (calc.getDeterminantSign(t1, t, points.get(i)) > 0) {
				stack.push(points.get(i));
			} else {
				stack.pop();
				i--;
			}
		}
		tool.clean();
		for (Vertex point3d : points) {
			if (!stack.contains(point3d)) {
				point3d.setGroupId(Utils.OTHERS);
				tool.addGeometry(point3d);
			}
		}
		for (Vertex point3d : removed) {
			tool.addGeometry(point3d);
		}

		visualiseStack(stack, true);
		tool.flushBuffer();
		tool.render();
		
		return new LinkedList<>(stack);
	}
	
	private void visualiseStack(Stack<Vertex> stack, boolean closed) {
		int to = closed ? stack.size() : stack.size()-1;
		for (int i=0; i<to; i++) {
			Vertex a = stack.get(i);
			Vertex b = stack.get((i+1)%stack.size());
			
			a.setGroupId(Utils.POINT_CH);
			tool.addGeometry(a);
			tool.addGeometry(Utils.pointsToEdge(a, b, Utils.CURRENT_CH));
		}
		stack.peek().setGroupId(Utils.POINT_CH);
		tool.addGeometry(stack.peek());
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
			
			if (calc.getDeterminantSign(a, initial, b) == 0) {
				points.remove(b);
				removed.add(b);
				i--;
			}
		}
		
		tool.clean();
		tool.addGeometry(initial);
		for (Vertex point3d : removed) {
			point3d.setGroupId(Utils.REMOVED);
			tool.addGeometry(point3d);
		}
		for (Vertex point3d : points) {
			point3d.setGroupId(Utils.OTHERS);
			tool.addGeometry(point3d);
		}
		tool.flushBuffer();
		tool.render();
		tool.breakpoint();
		//
	}
}
