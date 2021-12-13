package com.kessler.thechain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.vogella.algorithms.dijkstra.model.Edge;
import de.vogella.algorithms.dijkstra.model.Graph;
import de.vogella.algorithms.dijkstra.model.Vertex;

public class SynData {
	
	private Map<String, Set<String>>			syns = new LinkedHashMap<String, Set<String>>();
	
	public void readMobySyns(InputStream is) throws IOException
	{
		BufferedReader			reader = new BufferedReader(new InputStreamReader(is));
		String					line;
		
		while ( (line = reader.readLine()) != null )
		{
			Set<String>	wordSyns = null;
			
			for ( String word : line.split(",") )
			{
				if ( wordSyns == null )
					syns.put(word.intern(), wordSyns = new LinkedHashSet<String>());
				else
					wordSyns.add(word.intern());
			}
			
			/*
			if ( (syns.size() % 1000) == 0 )
				System.out.println("readMobySyns: " + syns.size());
			*/
		}
		
		/*
		System.out.println("readMobySyns: " + syns.size());
		*/
	}

	public Set<String> getWordSyns(String word)
	{
		return syns.get(word);
	}
	
	public Graph getSynGraph()
	{
		Map<String, Vertex>		nodesByName = new LinkedHashMap<String, Vertex>();
		List<Edge>				edges = new ArrayList<Edge>();
	    
	    // build nodes
	    for ( String word : syns.keySet() )
	    	nodesByName.put(word, new Vertex(Integer.toString((nodesByName.size() + 1)), word));
	    //System.out.println("nodesByName: " + nodesByName.size());
	    
	    // build edges
	    for ( String word : syns.keySet() )
	    {
	    	Vertex			src = nodesByName.get(word);
	    	
	    	for ( String synWord : syns.get(word) )
	    	{
	    		Vertex		dst = nodesByName.get(synWord);
	    		if ( dst == null )
	    			nodesByName.put(synWord, dst = new Vertex(Integer.toString((nodesByName.size() + 1)), synWord));
	    		
		    	edges.add(new Edge(Integer.toString(edges.size()), src, dst, 1));
	    	}
	    }
	    //System.out.println("nodesByName: " + nodesByName.size());
	    //System.out.println("edges: " + edges.size());

        return new Graph(new ArrayList<Vertex>(nodesByName.values()), edges);
	}
	
}
