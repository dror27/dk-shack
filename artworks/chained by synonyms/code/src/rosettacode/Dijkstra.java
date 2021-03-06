package rosettacode;

 
public class Dijkstra {
   private static final Graph.Edge[] GRAPH = {
      new Graph.Edge("a", "b", 7),
      new Graph.Edge("a", "c", 9),
      new Graph.Edge("a", "f", 14),
      new Graph.Edge("b", "c", 10),
      new Graph.Edge("b", "d", 15),
      new Graph.Edge("c", "d", 11),
      new Graph.Edge("c", "f", 2),
      new Graph.Edge("d", "e", 6),
      new Graph.Edge("e", "f", 9),
   };
   private static final String START = "a";
   private static final String END = "e";
 
   public static void main(String[] args) {
      Graph g = new Graph(GRAPH, 0, 0);
      g.dijkstra(START, null);
      g.printPath(END);
      //g.printAllPaths();
   }
}
 