package rosettacode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

public  class Graph {
	   private final Map<String, Vertex> graph; // mapping of vertex names to Vertex objects, built from a set of Edges
	 
	   /** One edge of the graph (only used by Graph constructor) */
	   public static class Edge {
	      @Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((v1 == null) ? 0 : v1.hashCode());
			result = prime * result + ((v2 == null) ? 0 : v2.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Edge other = (Edge) obj;
			if (v1 == null) {
				if (other.v1 != null)
					return false;
			} else if (!v1.equals(other.v1))
				return false;
			if (v2 == null) {
				if (other.v2 != null)
					return false;
			} else if (!v2.equals(other.v2))
				return false;
			return true;
		}
		public final String v1, v2;
	      public final int dist;
	      public Edge(String v1, String v2, int dist) {
	         this.v1 = v1;
	         this.v2 = v2;
	         this.dist = dist;
	      }
	   }
	 
	   /** One vertex of the graph, complete with mappings to neighbouring vertices */
	  public static class Vertex implements Comparable<Vertex>{
		public final String name;
		public int dist = Integer.MAX_VALUE; // MAX_VALUE assumed to be infinity
		public Vertex previous = null;
		public final Map<Vertex, Integer> neighbours = new HashMap<Vertex, Integer>();
	 
		public Vertex(String name)
		{
			this.name = name;
		}
	 
		private List<String> printPath()
		{
			if (this == this.previous)
			{
				//System.out.printf("%s", this.name);
				
				List<String>	l = new LinkedList<String>();
				l.add(this.name);
				return l;
			}
			else if (this.previous == null)
			{
				//System.out.printf("%s(unreached)", this.name);
				
				List<String>	l = new LinkedList<String>();
				l.add(this.name);
				return l;
			}
			else
			{
				List<String>	l = this.previous.printPath();
				//System.out.printf(" -> %s(%d)", this.name, this.dist);
				//System.out.printf(" -> %s", this.name);
				
				l.add(this.name);
				return l;
			}
		}
	 
		public int compareTo(Vertex other)
		{
			if (dist == other.dist)
				return name.compareTo(other.name);
	 
			//return Integer.compare(dist, other.dist);
			return dist - other.dist;
		}
	 
		@Override public String toString()
		{
			return "(" + name + ", " + dist + ")";
		}
	}
	 
	   /** Builds a graph from a set of edges */
	   public Graph(Edge[] edges, int start, int length) {
		  if ( length == 0 )
			  length = edges.length;
	      graph = new HashMap<String,Vertex>(length);
	 
	      //one pass to find all vertices
	      int			lastOfs = start + length;
	      for ( int ofs = start ; ofs < lastOfs ; ofs++ ) {
	    	 Edge e = edges[ofs];
	         if (!graph.containsKey(e.v1)) graph.put(e.v1, new Vertex(e.v1));
	         if (!graph.containsKey(e.v2)) graph.put(e.v2, new Vertex(e.v2));
	      }
	 
	      //another pass to set neighbouring vertices
	      for ( int ofs = start ; ofs < lastOfs ; ofs++ ) {
	    	 Edge e = edges[ofs];
	         graph.get(e.v1).neighbours.put(graph.get(e.v2), e.dist);
	         //graph.get(e.v2).neighbours.put(graph.get(e.v1), e.dist); // also do this for an undirected graph
	      }
	   }
	 
	   /** Runs dijkstra using a specified source vertex */ 
	   public void dijkstra(String startName, String endName) {
	      if (!graph.containsKey(startName)) {
	         //System.err.printf("Graph doesn't contain start vertex \"%s\"\n", startName);
	         return;
	      }
	      final Vertex source = graph.get(startName);
	      NavigableSet<Vertex> q = new TreeSet<Vertex>();
	 
	      // set-up vertices
	      for (Vertex v : graph.values()) {
	         v.previous = v == source ? source : null;
	         v.dist = v == source ? 0 : Integer.MAX_VALUE;
	         q.add(v);
	      }
	 
	      dijkstra(q, endName);
	   }
	 
	   /** Implementation of dijkstra's algorithm using a binary heap. */
	   private void dijkstra(final NavigableSet<Vertex> q, final String endName) {      
	      Vertex u, v;
	      while (!q.isEmpty()) {
	 
	         u = q.pollFirst(); // vertex with shortest distance (first iteration will return source)
	         if (u.dist == Integer.MAX_VALUE) break; // we can ignore u (and any other remaining vertices) since they are unreachable
	         
	         //look at distances to each neighbour
	         for (Map.Entry<Vertex, Integer> a : u.neighbours.entrySet()) {
	            v = a.getKey(); //the neighbour in this iteration
	 
	            final int alternateDist = u.dist + a.getValue();
	            if (alternateDist < v.dist) { // shorter path to neighbour found
	               q.remove(v);
	               v.dist = alternateDist;
	               v.previous = u;
	               q.add(v);
	            } 
	         }
	         
	         // stop on endName?
	         if ( endName != null && endName.equals(u.name) )
	        	 break;
	      }
	   }
	 
	   /** Prints a path from the source to the specified vertex */
	   public List<String> printPath(String endName) {
	      if (!graph.containsKey(endName)) {
	         //System.err.printf("Graph doesn't contain end vertex \"%s\"\n", endName);
	         return null;
	      }
	 
	      List<String> 	l = graph.get(endName).printPath();
	      /*
	      if ( l.size() > 1 )
	    	  System.out.println();
	   		*/
	      return l;
	   }
	   /** Prints the path from the source to every vertex (output order is not guaranteed) */
	   public void printAllPaths() {
	      for (Vertex v : graph.values()) {
	         v.printPath();
	         System.out.println();
	      }
	   }
	}