package app;

import java.util.LinkedList;
import java.util.Map;

import geometry.Vertex;

public interface CHService {
	public LinkedList<Vertex> computeCH(LinkedList<Vertex> points);
	public Map<String, Long> getTimes();
}
