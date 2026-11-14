package lastbastion.hive;
import java.util.ArrayList;
import java.util.List;

/** Bounded triangular formation: every upper participant has two living supports. */
public final class PyramidPlan {
 public record Slot(int id,int tier,double x,double y,double z,int leftSupport,int rightSupport){}
 private PyramidPlan(){}
 public static int widthForNight(long night){return night<15?0:night<35?2:night<60?3:night<85?4:5;}
 public static List<Slot> create(int width,double x,double y,double z,double forwardX,double forwardZ){
  if(width<2||width>5||!Double.isFinite(x)||!Double.isFinite(y)||!Double.isFinite(z)||!Double.isFinite(forwardX)||!Double.isFinite(forwardZ))throw new IllegalArgumentException();
  double length=Math.hypot(forwardX,forwardZ);if(length<0.001)throw new IllegalArgumentException();
  double fx=forwardX/length,fz=forwardZ/length;
  var slots=new ArrayList<Slot>();int previousStart=-1;
  for(int tier=0;tier<width;tier++){
   int count=width-tier,start=slots.size();
   for(int i=0;i<count;i++){
    double sideways=(i-(count-1)/2.0)*0.82,advance=tier*0.18;
    slots.add(new Slot(slots.size(),tier,x-fz*sideways+fx*advance,y+tier*1.95,z+fx*sideways+fz*advance,
     tier==0?-1:previousStart+i,tier==0?-1:previousStart+i+1));
   }
   previousStart=start;
  }
  return List.copyOf(slots);
 }
}
