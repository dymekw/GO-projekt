package util;

import java.util.Comparator;

import geometry.Point3D;
import geometry.Vertex;

public class PointComparator implements Comparator<Vertex> {
	
	private Point3D initial;
	private static final DeterminantCalc calc = new DeterminantCalc();
	
	public PointComparator(Vertex initial) {
		this.initial = initial.getPoint();
	}

	@Override
	public int compare(Vertex o1, Vertex o2) {
		int deter = calc.getDeterminantSign(o1.getPoint(), initial, o2.getPoint());
		
		if (deter == 0) {
			double v1 = Utils.getDistance(o1.getPoint(), initial);
			double v2 = Utils.getDistance(o2.getPoint(), initial);
			
			return (int)Math.signum(v2-v1);
		}
		
		return deter;
	}
	
}
