package lastbastion.hive.routing;
/** Breaks a distant observation into bounded native path requests, without changing the observation. */
public final class LocalRouteLeg {
 public record Position(double x,double y,double z){
  public Position{if(!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z))throw new IllegalArgumentException("Nonfinite route position");}
 }
 private LocalRouteLeg(){}
 public static Position toward(Position from,Position goal){
  double dx=goal.x-from.x,dy=goal.y-from.y,dz=goal.z-from.z;
  double horizontal=Math.hypot(dx,dz),scale=1;
  if(horizontal>32)scale=32/horizontal;
  if(Math.abs(dy)>8)scale=Math.min(scale,8/Math.abs(dy));
  return scale==1?goal:new Position(from.x+dx*scale,from.y+dy*scale,from.z+dz*scale);
 }
}
