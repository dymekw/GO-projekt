package jarvis;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import app.CHService;
import geometry.Edge;
import geometry.Vertex;
import tool.Smeshalist;
import util.PointComparator;
import util.Utils;

public class Jarvis implements CHService {
	
	private static final Smeshalist tool = Smeshalist.getInstance();
	private static int sleepTime = 5000;

	@Override
	public LinkedList<Vertex> computeCH(LinkedList<Vertex> points) {
		Vertex initial = Utils.getInitialPoint(points, false);
		List<Vertex> list = new LinkedList<>();
		
		list.add(initial);
		Vertex current = initial;
		
		for (Vertex point3d : points) {
			if (point3d != initial) {
				point3d.setGroupId(Utils.OTHERS);
			} else {
				point3d.setGroupId(Utils.INITIAL);
			}
			tool.addGeometry(point3d);
		}
		tool.flushBuffer();
		tool.clean();
		tool.render();
		tool.breakpoint();
		
		int step = 0;
		do {
			PointComparator comparator = new PointComparator(current);
			points.sort(comparator);
			
			current = points.get(0);
			
			list.add(current);
			visualiseCH(list, points, step++);
		} while (current != initial);
		
		return new LinkedList<Vertex>(list.subList(0, list.size()-2));
	}

	private void visualiseCH(List<Vertex> list, List<Vertex> points, int step) {
		for (Vertex point3d : list) {
			point3d.setGroupId(Utils.POINT_CH);
		}
		
		for (int i=0; i<list.size()-1; i++) {
			Edge e = Utils.pointsToEdge(list.get(i), list.get(i+1), Utils.CURRENT_CH);
			tool.addGeometry(e);
		}
		
		for (Vertex point3d : points) {
			tool.addGeometry(point3d);
		}
		
		tool.flushBuffer();
		tool.clean();
		tool.render();
		
		if (step == 0) {
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
	}
}
