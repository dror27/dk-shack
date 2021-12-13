package com.kessler.thechain;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Set;

import de.vogella.algorithms.dijkstra.engine.DijkstraAlgorithm;
import de.vogella.algorithms.dijkstra.model.Graph;
import de.vogella.algorithms.dijkstra.model.Vertex;

public class TheChain {
	
	static SynData		synData = new SynData();

	public static void main(String[] args) throws IOException
	{
		// read database
		synData.readMobySyns(new FileInputStream(args[0]));
		
		// show syns for word
		printWordSyns(args[1]);
		printWordSyns(args[2]);
		
		// make a graph
		Graph		graph = synData.getSynGraph();
		Vertex		v1 = graph.getVertexByName(args[1]);
		System.out.println("v1: " + v1);
		Vertex		v2 = graph.getVertexByName(args[2]);
		System.out.println("v2: " + v2);
		
		// find path
        DijkstraAlgorithm 	dijkstra = new DijkstraAlgorithm(graph);
        System.out.println("dijkstra created");
        dijkstra.execute(v1);
        System.out.println("dijkstra executed with v1: " + v1);
        LinkedList<Vertex> path = dijkstra.getPath(v2);
        System.out.println("dijkstra path found to v2: " + v2);
        System.out.println("path: " + path);
	}

	private static void printWordSyns(String word)
	{
		Set<String>		wordSyns = synData.getWordSyns(word);
		if ( wordSyns == null )
			System.out.println("no syns for: " + word);
		else
		{
			for ( String syn : wordSyns )
			{
				System.out.print(syn);
				System.out.print(' ');
			}
			System.out.println("");
		}		
	}
}
