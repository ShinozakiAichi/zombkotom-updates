package lastbastion.hive.routing;
import java.util.List;
/** Short-lived snapshot of observed bodies, not reservations or knowledge through walls. */
public final class CrowdingField {
 public record Point(double x,double y,double z){
  public Point{if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z))throw new IllegalArgumentException("Nonfinite body location");}
 }
 private final List<Point> bodies;
 public CrowdingField(List<Point> bodies){if(bodies.size()>32)throw new IllegalArgumentException("Crowd sample cap");this.bodies=List.copyOf(bodies);}
 public double density(double x,double y,double z){
  double value=0;
  for(var body:bodies){double dx=x-body.x,dy=y-body.y,dz=z-body.z,d2=dx*dx+dy*dy+dz*dz;if(d2<9)value+=1-d2/9;}
  return Math.min(8,value);
 }
}
