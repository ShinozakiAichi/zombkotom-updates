import com.google.gson.*;
import java.nio.file.*;
import lastbastion.client.AnimationJsonRepair;
public class AnimationRepairTest {
 static int checks;
 static void check(boolean b,String msg){if(!b)throw new AssertionError(msg);checks++;}
 public static void main(String[] args)throws Exception{
  for(String file:new String[]{"qbz_95","vector","clogger"}){
   var before=JsonParser.parseString(Files.readString(Path.of(args[0],file+".animation.json")));
   var original=before.deepCopy();var after=AnimationJsonRepair.repair(before);
   check(before.equals(original),file+" original not mutated");check(!after.equals(before),file+" defective fixture repaired");
   check(AnimationJsonRepair.repair(after)==after,file+" repair idempotent");
   var b=before.getAsJsonObject().getAsJsonObject("animations");var a=after.getAsJsonObject().getAsJsonObject("animations");
   var nativeInput=b.deepCopy();var nativeOutput=AnimationJsonRepair.repairAnimations(b);
   check(nativeOutput.equals(a),"native GeckoLib inner-map input repaired "+file);
   check(b.equals(nativeInput),"native input remains unmodified "+file);
   check(AnimationJsonRepair.repairAnimations(nativeOutput)==nativeOutput,"native repair idempotent "+file);
   String key=file.equals("clogger")?"ded":"animation."+file+".edit";
   for(String name:b.keySet())if(!name.equals(key))check(b.get(name).equals(a.get(name)),file+" unrelated clip "+name);
   var bb=b.getAsJsonObject(key).getAsJsonObject("bones");var ab=a.getAsJsonObject(key).getAsJsonObject("bones");
   if(file.equals("clogger")){
    check(ab.getAsJsonObject("head").getAsJsonObject("position").get("vector").toString().equals("[0,0,0]"),"clogger neutral head position");
    for(String bone:bb.keySet())if(!bone.equals("head"))check(bb.get(bone).equals(ab.get(bone)),"clogger unchanged "+bone);
   }else{
    for(String bone:new String[]{"Lefthand","Righthand"}){
     check(ab.getAsJsonObject(bone).get("scale").getAsDouble()==0,"hidden hands remain hidden");
     for(String channel:new String[]{"position","rotation"})for(var v:ab.getAsJsonObject(bone).getAsJsonArray(channel))check(v.getAsJsonPrimitive().isNumber(),"valid numeric hand vector");
    }
    check(bb.get("0").equals(ab.get("0")),"weapon motion unchanged");
   }
  }
  var normal=JsonParser.parseString("{\"animations\":{\"other\":{\"bones\":{\"arm\":{\"position\":[\"query.anim_time\",0,0]}}}}}");
  check(AnimationJsonRepair.repair(normal)==normal,"valid Molang untouched");
  check(AnimationJsonRepair.repair(JsonNull.INSTANCE)==JsonNull.INSTANCE,"null unchanged");
  check(AnimationJsonRepair.repair(null)==null,"Java null unchanged");
  System.out.println("ANIMATION_REPAIR_TEST PASS "+checks);
 }
}
