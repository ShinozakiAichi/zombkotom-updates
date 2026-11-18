package lastbastion.hive.routing;

import java.util.*;

/** Small directed tactical graph. All weights use the caller's common time/risk scale. */
public final class RouteGraph {
 public static final int MAX_NODES=64,MAX_EDGES=256;
 public enum Mode { WALK,LADDER,WAIT_GATE }
 public record Node(String id,double x,double y,double z){
  public Node {name(id);if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z))throw new IllegalArgumentException("Nonfinite position");}
 }
 public record Cost(double travel,double exposure,double crowding,double preparation,double uncertainty){
  public Cost {
   for(double value:new double[]{travel,exposure,crowding,preparation,uncertainty})
    if(!Double.isFinite(value)||value<0)throw new IllegalArgumentException("Negative or nonfinite cost");
   if(!Double.isFinite(travel+exposure+crowding+preparation+uncertainty))throw new IllegalArgumentException("Cost overflow");
  }
  public double total(){return travel+exposure+crowding+preparation+uncertainty;}
 }
 public record Edge(String from,String to,Cost cost,Mode mode,int requiredMembers){
  public Edge {name(from);name(to);Objects.requireNonNull(cost);Objects.requireNonNull(mode);if(requiredMembers<1)throw new IllegalArgumentException("Members must be positive");}
 }
 public record Route(List<String> nodes,List<Edge> edges,double cost){
  public Route {nodes=List.copyOf(nodes);edges=List.copyOf(edges);}
 }
 private record Visit(String node,double cost){}
 private final Map<String,Node> nodes;
 private final Map<String,List<Edge>> outgoing;
 public RouteGraph(Collection<Node> inputNodes,Collection<Edge> inputEdges){
  if(inputNodes.isEmpty()||inputNodes.size()>MAX_NODES||inputEdges.size()>MAX_EDGES)throw new IllegalArgumentException("Graph capacity");
  var byId=new TreeMap<String,Node>();
  for(var node:inputNodes)if(byId.put(node.id(),node)!=null)throw new IllegalArgumentException("Duplicate node");
  var adjacency=new TreeMap<String,List<Edge>>();
  for(var edge:inputEdges){
   if(!byId.containsKey(edge.from())||!byId.containsKey(edge.to()))throw new IllegalArgumentException("Unknown endpoint");
   adjacency.computeIfAbsent(edge.from(),key->new ArrayList<>()).add(edge);
  }
  var order=Comparator.comparing(Edge::to).thenComparing(Edge::mode).thenComparingDouble(e->e.cost().total()).thenComparingInt(Edge::requiredMembers);
  adjacency.replaceAll((key,edges)->edges.stream().sorted(order).toList());
  nodes=Map.copyOf(byId);outgoing=Map.copyOf(adjacency);
 }
 private static void name(String value){if(value==null||value.isBlank())throw new IllegalArgumentException("Missing node id");}
 public Optional<Route> shortestPath(String start,String goal,int availableMembers){
  if(!nodes.containsKey(start)||!nodes.containsKey(goal)||availableMembers<1)throw new IllegalArgumentException("Invalid query");
  var distances=new HashMap<String,Double>();var previous=new HashMap<String,Edge>();var settled=new HashSet<String>();
  var queue=new PriorityQueue<Visit>(Comparator.comparingDouble(Visit::cost).thenComparing(Visit::node));
  distances.put(start,0.0);queue.add(new Visit(start,0));
  while(!queue.isEmpty()){
   var visit=queue.remove();
   if(visit.cost()>distances.getOrDefault(visit.node(),Double.POSITIVE_INFINITY)||!settled.add(visit.node()))continue;
   if(visit.node().equals(goal)){
    var edges=new ArrayList<Edge>();String at=goal;
    while(!at.equals(start)){var edge=previous.get(at);edges.add(edge);at=edge.from();}
    Collections.reverse(edges);var path=new ArrayList<String>();path.add(start);for(var edge:edges)path.add(edge.to());
    return Optional.of(new Route(path,edges,visit.cost()));
   }
   for(var edge:outgoing.getOrDefault(visit.node(),List.of())){
    if(edge.requiredMembers()>availableMembers||settled.contains(edge.to()))continue;
    double next=visit.cost()+edge.cost().total();
    if(!Double.isFinite(next)||next>=distances.getOrDefault(edge.to(),Double.POSITIVE_INFINITY))continue;
    distances.put(edge.to(),next);previous.put(edge.to(),edge);queue.add(new Visit(edge.to(),next));
   }
  }
  return Optional.empty();
 }
}
