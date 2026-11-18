package lastbastion.hive.routing;
import lastbastion.hive.PyramidPlan;
import java.util.*;

/** Cardinal wall geometry; normal points outward from the observed wall face. */
public final class WallLadderGeometry {
 public record Plan(List<PyramidPlan.Slot> slots,int width,int wallX,int topY,int wallZ){}
 private WallLadderGeometry(){}
 public static Optional<Plan> plan(int wallX,int groundY,int wallZ,int height,int outwardX,int outwardZ,long night,int available){
  if(Math.abs(outwardX)+Math.abs(outwardZ)!=1||height<2||height>9)return Optional.empty();
  int width=Math.max(2,1+(int)Math.ceil((height-1.2)/1.95));
  if(width>PyramidPlan.widthForNight(night)||width*(width+1)/2>available)return Optional.empty();
  double back=1.3+(width-1)*.18;
  var slots=PyramidPlan.create(width,wallX+.5+outwardX*back,groundY,wallZ+.5+outwardZ*back,-outwardX,-outwardZ);
  return Optional.of(new Plan(slots,width,wallX,groundY+height,wallZ));
 }
}
