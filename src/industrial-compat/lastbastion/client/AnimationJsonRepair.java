package lastbastion.client;

import com.google.gson.*;

/** Repairs known malformed exported vectors without copying third-party animation assets. */
public final class AnimationJsonRepair {
 private AnimationJsonRepair() {}
 /** GeckoLib passes the inner animations map, not the document root. */
 public static JsonElement repairAnimations(JsonElement animations) {
  if(animations==null || !animations.isJsonObject())return animations;
  var wrapper=new JsonObject();wrapper.add("animations",animations);
  var fixed=repair(wrapper);
  return fixed==wrapper?animations:fixed.getAsJsonObject().get("animations");
 }
 public static JsonElement repair(JsonElement input) {
  if(input==null || !input.isJsonObject())return input;
  var root=input.getAsJsonObject();
  if(!root.has("animations") || !root.get("animations").isJsonObject())return input;
  var copy=root.deepCopy();boolean changed=false;
  var animations=copy.getAsJsonObject("animations");
  for(String name:new String[]{"animation.qbz_95.edit","animation.vector.edit"}){
   var a=object(animations,name);var bones=object(a,"bones");
   for(String bone:new String[]{"Lefthand","Righthand"}){
    var b=object(bones,bone);
    // The author's hidden hands retain their scale=0; neutral vectors cannot alter visible hand motion.
    if(b==null || !b.has("scale") || !b.get("scale").isJsonPrimitive() || !b.getAsJsonPrimitive("scale").isNumber() || b.get("scale").getAsDouble()!=0)continue;
    for(String channel:new String[]{"rotation","position"})if(badVector(b.get(channel),"easeInElastic")){
     b.add(channel,zero());changed=true;
    }
   }
  }
  var dead=object(animations,"ded");var bones=object(dead,"bones");
  // Clogger's exported death pose. All limb transforms stay intact; only an invalid head offset is neutralized.
  if(bones!=null && bones.has("whole") && bones.has("leg0") && bones.has("leg3") && bones.has("jaw")){
   var position=object(object(bones,"head"),"position");
   if(position!=null && badVector(position.get("vector"),"easeInBounce")){position.add("vector",zero());changed=true;}
  }
  return changed?copy:input;
 }
 private static JsonObject object(JsonObject o,String key){return o!=null && o.has(key) && o.get(key).isJsonObject()?o.getAsJsonObject(key):null;}
 private static boolean badVector(JsonElement value,String bad){
  if(value==null || !value.isJsonArray() || value.getAsJsonArray().size()!=3)return false;
  for(var v:value.getAsJsonArray())if(!v.isJsonPrimitive() || !v.getAsJsonPrimitive().isString() || !bad.equals(v.getAsString()))return false;
  return true;
 }
 private static JsonArray zero(){var a=new JsonArray();a.add(0);a.add(0);a.add(0);return a;}
}
