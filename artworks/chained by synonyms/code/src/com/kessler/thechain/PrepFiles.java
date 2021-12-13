package com.kessler.thechain;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import de.vogella.algorithms.dijkstra.model.Edge;
import de.vogella.algorithms.dijkstra.model.Graph;
import de.vogella.algorithms.dijkstra.model.Vertex;

public class PrepFiles {

	static SynData		synData = new SynData();

	public static void main(String[] args) throws IOException
	{
		// read database
		synData.readMobySyns(new FileInputStream(args[0]));
		
		// make a graph
		Graph		graph = synData.getSynGraph();
		
		// output nodes file
		PrintWriter	pw = new PrintWriter(new FileOutputStream(args[1]));
		for ( Vertex v : graph.getVertexes() )
			pw.println(String.format("%s %s", v.getId(), v.getName()));
		pw.close();
		System.out.println("nodes file written to: " + args[1]);
		
		// output graph file
		pw = new PrintWriter(new FileOutputStream(args[2]));
		pw.println(String.format("%d %d", graph.getVertexes().size(), graph.getEdges().size()));
		for ( Edge e : graph.getEdges() )
			pw.println(String.format("%s %s 1", e.getSource().getId(), e.getDestination().getId()));
		int		qNum = (args.length - 3) / 2;
		pw.println(qNum);
		for ( int i = 0 ; i < qNum ; i++ )
		{
			String		src = args[3 + i * 2];
			String		dst = args[3 + i * 2 + 1];
			
			Vertex		srcVertex = graph.getVertexByName(src);
			Vertex		dstVertex = graph.getVertexByName(dst);
			
			pw.println(String.format("%s %s", srcVertex.getId(), dstVertex.getId()));
		}
		
		pw.close();
		System.out.println("graph file written to: " + args[1]);
	}

}
