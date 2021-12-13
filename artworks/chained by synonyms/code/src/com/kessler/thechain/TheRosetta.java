package com.kessler.thechain;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;

import rosettacode.Graph;

public class TheRosetta {
	
	SynData											synData = new SynData();
	de.vogella.algorithms.dijkstra.model.Graph		graph;
	Graph.Edge[]									edges;
	double											iterationsPerDensity = 1000;
	double											density = 0.03;
	double											densityMin = 0.005;
	double											densityFactor =  0.95;
	boolean											silent = true;
	int												threadCount = 1;
	AtomicBoolean									stopMark = new AtomicBoolean();

	public static void main(String[] args) throws IOException
	{
		final TheRosetta	tr = new TheRosetta(new FileInputStream(args[0]));
		Random				random = new Random();
		
		// establish polls
		boolean			rand = false;
		String			p1 = args[1];
		String			p2 = args[2];
		if ( p1.equals("?") )
		{
			rand = true;
			do {
				p1 = tr.randomWord(random);
			} while (p1.contains(" ") || p1.contains("-"));
		}
		System.out.println("p1: " + p1);
		if ( p2.startsWith("?") )
		{
			rand = true;
			int		radius = p2.equals("?") ? 0 : Integer.parseInt(p2.substring(1));
			do {
				if ( radius == 0 )
					p2 = tr.randomWord(random);
				else
					p2 = tr.randomWord(random, p1, radius);
			} while (p2.contains(" ") || p2.contains("-") || p2.equals(p1));
		}
		System.out.println("p2: " + p2);
		
		// configure
		tr.iterationsPerDensity = Integer.parseInt(args[3]);
		tr.density = Double.parseDouble(args[4]);
		tr.densityMin = Double.parseDouble(args[5]);
		tr.densityFactor = Double.parseDouble(args[6]);
		final int				pathMax = Integer.parseInt(args[7]);
		
		// open output
		String					outfile = args[8].replace("(1)", p1).replace("(2)", p2);
		System.out.println("outfile: " + outfile);
		OutputStream			os = new FileOutputStream(outfile);
		final PrintWriter		pw = new PrintWriter(os);
		
		// run the app
		Monitor					mon = new Monitor("finding " + pathMax + " paths");
		tr.silent = !rand;
		tr.app(p1, p2, new IPathCollector() {
			
			int						pathCount = 0;

			@Override
			public synchronized boolean addPath(List<String> path) 
			{
				if ( pathCount < pathMax )
				{
					pathCount++;
					//pw.println(tr.pathAsString(path));
					pw.println(StringUtils.join(path, '/'));
					pw.flush();
				}
		    	
				return pathCount < pathMax;
			}
		});
		mon.done(false);
		
		// close output
		pw.close();
	}
	
	private String randomWord(Random random) 
	{
		List<de.vogella.algorithms.dijkstra.model.Vertex>		vertexs = graph.getVertexes();
		
		return vertexs.get(random.nextInt(vertexs.size())).getName();
	}

	private String randomWord(Random random, String centerWord, int radius) 
	{
		Set<String>			words = new HashSet<String>();
		
		collectSynsAroundWord(centerWord, words, radius);
		words.remove(centerWord);
		
		
		return words.toArray(new String[0])[random.nextInt(words.size())];
	}

	private void collectSynsAroundWord(String word, Set<String> words, int radius) 
	{
		if ( radius <- 0 )
			return;
		
		Set<String>		syns = synData.getWordSyns(word);
		if ( syns != null )
		{
			Set<String>		newSyns = new HashSet<String>(syns);
			newSyns.removeAll(words);
			words.addAll(newSyns);
			
			for ( String syn : newSyns )
				collectSynsAroundWord(syn, words, radius - 1);
		}
	}

	public TheRosetta(InputStream is) throws IOException
	{
		// read syn data
		readSynData(is);
	}
	
	public void readSynData(InputStream is) throws IOException
	{
		synData.readMobySyns(is);
		graph = synData.getSynGraph();

		List<Graph.Edge>	list = new ArrayList<Graph.Edge>();
		for ( de.vogella.algorithms.dijkstra.model.Edge edge : graph.getEdges() )
		{
			Graph.Edge		e = new Graph.Edge(edge.getSource().getName(), edge.getDestination().getName(), edge.getWeight());
			
			list.add(e);
		}
		
		edges = list.toArray(new Graph.Edge[0]);
	}
	
	public void app(final String p1, final String p2, final IPathCollector pc) throws IOException
	{
		// show syns for polls
		// Monitor.setSilent(silent);
		if ( !silent )
		{
			printWordSyns(p1);
			printWordSyns(p2);
		}
		
		for ( ; density > densityMin ; density *= densityFactor )
		{
			if ( !silent )
				System.out.println("density: " + density);
			
			// setup threads
			final int		iterPerThread = (int)iterationsPerDensity / threadCount;
			Thread[]		threads = new Thread[threadCount];
			for ( int threadIndex = 0 ; threadIndex < threadCount ; threadIndex++ )
				threads[threadIndex] = new Thread(new Runnable() {
					
					@Override
					public void run() 
					{
						Monitor			m = new Monitor("");
						worker(p1, p2, pc, iterPerThread);
						
						m.done(false, Thread.currentThread().getName() + ": done, iterPerThread: " + iterPerThread + ", density: " + density);
					}
				});
			
			// start
			for ( int threadIndex = 0 ; threadIndex < threadCount ; threadIndex++ )
			{
				threads[threadIndex].setName("Rosetta-" + threadIndex);
				threads[threadIndex].start();
			}
			
			// join
			for ( int threadIndex = 0 ; threadIndex < threadCount ; threadIndex++ )
			{
				try {
					threads[threadIndex].join();
				} catch (InterruptedException e) {
					throw new IOException(e);
				}
			}
			
			// done?
			if ( stopMark.get() )
				return;
			
		}
		
	}
	
	private boolean worker(String p1, String p2, IPathCollector pc, int iterCount)
	{
		// select edges
		int					edgesSize = edges.length;
		int					desiredSize = (int)(edgesSize * density);
		int					selectedSets = edgesSize / desiredSize;
		int					lastRemainder = (edgesSize - desiredSize * selectedSets);
		Graph.Edge[]		selectedEdges = new Graph.Edge[edgesSize]; 
		Random				random = new Random(Thread.currentThread().getName().hashCode());
		int					foundCount = 0;
		
		for ( int iter = 0 ; iter < iterCount ; iter += selectedSets )
		{
			setupSelectedEdges(selectedEdges, random);
			
			for ( int setIndex = 0 ; setIndex < selectedSets ; setIndex++ )
			{
				int		  	setStart = setIndex * desiredSize;
				int			setLength = desiredSize + ((setIndex == (selectedSets - 1)) ? lastRemainder : 0);
				
				Monitor		m = new Monitor("build rosseta graph");
				Graph g = new Graph(selectedEdges, setStart, setLength);
				m.done();
				
				// find path
				m = new Monitor("dijkstra");
				g.dijkstra(p1, p2);
				m.done();
				m = new Monitor("printPath");
			    List<String>		path = g.printPath(p2);
			    m.done();
			    if ( path == null || path.size() <= 1 )
			    	continue;
			    
			    if ( pc != null )
			    	if ( !pc.addPath(path) )
			    	{
			    		stopMark.set(true);
			    		return false;
			    	}
			    if ( !silent )
			    	System.out.println(pathAsString(path));
			    
			    foundCount++;
			}
		}
	    
		if ( foundCount != 0 )
			return true;
		else
		{
			stopMark.set(true);
    		return false;
		}
	}
	
	private void setupSelectedEdges(Graph.Edge[] selectedEdges, Random random)
	{

		Monitor		m = new Monitor("select edges");

		System.arraycopy(edges, 0, selectedEdges, 0, selectedEdges.length);
		shuffleEdges(selectedEdges, random);
		
		m.done();
	}

	private void shuffleEdges(Graph.Edge[] array, Random random)
	{
	    int 			index;
	    Graph.Edge		temp;
	    
	    for (int i = array.length - 1; i > 0; i--)
	    {
	        index = random.nextInt(i + 1);
	        temp = array[index];
	        array[index] = array[i];
	        array[i] = temp;
	    }
	}
	
	public String pathAsString(List<String> path) 
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

	private void printWordSyns(String word)
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
