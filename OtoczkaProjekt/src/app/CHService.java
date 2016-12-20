package app;

import java.util.LinkedList;

import geometry.Vertex;

public interface CHService {
	public LinkedList<Vertex> computeCH(LinkedList<Vertex> points);
}
