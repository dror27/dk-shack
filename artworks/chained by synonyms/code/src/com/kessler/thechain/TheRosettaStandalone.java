package com.kessler.thechain;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import rosettacode.Graph;

public class TheRosettaStandalone {
	
	static SynData		synData = new SynData();

	public static void main(String[] args) throws IOException
	{
		// read database
		synData.readMobySyns(new FileInputStream(args[0]));
		
		// show syns for word
		printWordSyns(args[1]);
		printWordSyns(args[2]);
		
		// make a graph
		de.vogella.algorithms.dijkstra.model.Graph		graph = synData.getSynGraph();
		System.out.println("v1: " + graph.getVertexByName(args[1]));
		System.out.println("v2: " + graph.getVertexByName(args[2]));
		
		// start with an empty black list
		Set<Graph.Edge>			blackList = new LinkedHashSet<Graph.Edge>();
		Random					random = new Random(0);
		double					density = Double.parseDouble(args[4]);
		double					densityMin = Double.parseDouble(args[5]);
		double					densityFactor = Double.parseDouble(args[6]);
		int						failCounter = 0;
		int						failCounterThreshold = 10;
		boolean					useBlackList = false;	
		int						pathCount;
		
		OutputStream			os = new FileOutputStream(args[7]);
		PrintWriter				pw = new PrintWriter(os);
		
		for ( pathCount = 0 ; density > densityMin ; density *= densityFactor, pathCount = 0 )
		{
			System.out.println("density: " + density);
			
			for ( int iter = 0 ; iter < Integer.parseInt(args[3]) ; iter++ )
			{
				//System.out.println("blackList: " + blackList);
				
				// build a rosseta graph
				List<Graph.Edge>	edges = new ArrayList<Graph.Edge>();
				for ( de.vogella.algorithms.dijkstra.model.Edge edge : graph.getEdges() )
				{
					double		r = random.nextDouble();
					if ( r > density )
						continue;
					
					Graph.Edge		e = new Graph.Edge(edge.getSource().getName(), edge.getDestination().getName(), edge.getWeight());
					if ( blackList.contains(e) )
						continue;
					
					edges.add(e);
				}
				
				Graph g = new Graph(edges.toArray(new Graph.Edge[0]), 0, 0);
				g.dijkstra(graph.getVertexByName(args[1]).getName(), null);
			    List<String>		path = g.printPath(graph.getVertexByName(args[2]).getName());
			    if ( path == null || path.size() <= 1 )
			    	continue;
			    
			    pw.println(pathAsString(path));
			    pw.flush();
			    os.flush();
			    System.out.println(pathAsString(path));
			    pathCount++;
			    if ( (pathCount % 50) == 0 )
			    	System.out.println("pathCount: " + pathCount);
			    
			    
			    // put all elements except first and last into the black list
			    if ( useBlackList )
			    {
				    for ( int i = 1 ; i < path.size() - 1 ; i++ )
				    {
				    	Graph.Edge		e = new Graph.Edge(path.get(i - 1).intern(), path.get(i).intern(), 1);
				    	Graph.Edge		e1 = new Graph.Edge(path.get(i).intern(), path.get(i + 1).intern(), 1);
				    	
				    	blackList.add(e);
				    	blackList.add(e1);
				    }
				    
				    // failed?
				    if ( path.size() <= 1 )
				    	failCounter++;
				    else
				    	failCounter = 0;
				    if ( failCounter >= failCounterThreshold )
				    {
				    	//System.out.println("** fail reset ** ");
				    	blackList.clear();
				    	failCounter = 0;
				    }
			    }
			}
			
			System.out.println("pathCount: " + pathCount);
		}
		
		pw.close();
	}

	private static String pathAsString(List<String> path) 
	{
		StringBuilder		sb = new StringBuilder();
		
		sb.append(String.format("%3d " , path.size()));
		boolean				first = true;
		for ( String s : path )
		{
			if ( !first )
				sb.append(" -> ");
			sb.append(s);
			
			first = false;
		}
		
		return sb.toString();
	}

	private static void printWordSyns(String word)
	{
		Set<String>		wordSyns = synData.getWordSyns(word);
		if ( wordSyns == null )
			System.out.println("no syns for: " + word);
		else
		{
			System.out.print(word + ": ");

			for ( String syn : wordSyns )
			{
				System.out.print(syn);
				System.out.print(", ");
			}
			System.out.println("");
		}		
	}
}
