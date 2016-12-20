package divideandconquer;

import java.util.LinkedList;
import java.util.List;

import geometry.Vertex;
import util.DeterminantCalc;

public class TangentService {
	private static DeterminantCalc calc = new DeterminantCalc();
	
	public boolean isTangent(Vertex a, Vertex b, List<Vertex> A, boolean leftTangent) {
		int sign = 0;
		
		List<Vertex> neighbours = new LinkedList<>();
		int index = A.indexOf(a);
		neighbours.add(A.get((index+1) % A.size()));
		neighbours.add(A.get(index > 0 ? index-1 : A.size()-1));
		
		for (Vertex point3d : neighbours) {
			int currSign = calc.getDeterminantSign(b, a, point3d);
			
			if (sign == 0 && currSign != 0) {
				sign = currSign;
			}
			
			if (sign != currSign && currSign != 0) {
				return false;
			}
		}
		
		if (leftTangent) {
			return sign < 0;
		}
		
		return sign > 0;
	}
	
	public boolean isTangent(Vertex a, Vertex b, List<Vertex> A, List<Vertex> B, boolean upper) {
		return isTangent(a, b, A, !upper) && isTangent(b, a, B, upper);
	}
}