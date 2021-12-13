package de.vogella.algorithms.dijkstra.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Graph implements Comparator<Vertex> {
    private final List<Vertex> vertexes;
    private final List<Edge> edges;
    
    private List<Vertex> vertexesByName;

    public Graph(List<Vertex> vertexes, List<Edge> edges) {
        this.vertexes = vertexes;
        this.edges = edges;
    }

    public List<Vertex> getVertexes() {
        return vertexes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

	public Vertex getVertexByName(String name) 
	{
		if ( vertexesByName == null )
		{
			vertexesByName = new ArrayList<Vertex>(vertexes);
			Collections.sort(vertexesByName, this);
		}
		
		// find it
		Vertex	v = new Vertex("", name);
		int		index = Collections.binarySearch(vertexesByName, v, this);
		
		return (index >= 0) ? vertexesByName.get(index) : null;
	}

	@Override
	public int compare(Vertex arg0, Vertex arg1) 
	{
		return arg0.getName().compareTo(arg1.getName());
	}


}