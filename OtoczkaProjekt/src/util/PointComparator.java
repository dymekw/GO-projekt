package util;

import java.util.Comparator;

import geometry.Vertex;

public class PointComparator implements Comparator<Vertex> {
	
	private Vertex initial;
	private static final DeterminantCalc calc = new DeterminantCalc();
	
	public PointComparator(Vertex initial) {
		this.initial = initial;
	}

	@Override
	public int compare(Vertex o1, Vertex o2) {
		int deter = calc.getDeterminantSign(o1, initial, o2);
		
		if (deter == 0) {
			double v1 = Utils.getDistance(o1.getPoint(), initial.getPoint());
			double v2 = Utils.getDistance(o2.getPoint(), initial.getPoint());
			
			return (int)Math.signum(v2-v1);
		}
		
		return deter;
	}
	
}
