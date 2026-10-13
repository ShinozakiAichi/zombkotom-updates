package ntglloadqa;
import java.util.*;
import java.util.function.Predicate;
import java.lang.reflect.*;
import java.nio.file.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import com.google.gson.GsonBuilder;

/** Calls the installed NTGL code with real server resources; never ships. */
@Mod("ntgl_loading_qa")
public class NtglLoadingProbe {
 public NtglLoadingProbe(){NeoForge.EVENT_BUS.addListener(this::commands);}
 void commands(RegisterCommandsEvent e){e.getDispatcher().register(Commands.literal("ntglqa").requires(s->s.hasPermission(4)).executes(c->{
  try {
   var server=c.getSource().getServer();var resources=server.getResourceManager();
   var result=new LinkedHashMap<String,Object>();var configs=new TreeMap<String,Object>();
   for(String name:List.of("Equipment","Chassis","Weapon","Ammo","Attachment","Projectile")){
    var cls=Class.forName("com.nukateam.ntgl.modules.datapack.managers.Network"+name+"Manager");
    Object manager=cls.getMethod("get").invoke(null);var values=new TreeMap<String,String>();
    if(manager!=null)for(Field f:cls.getDeclaredFields())if(!Modifier.isStatic(f.getModifiers())&&Map.class.isAssignableFrom(f.getType())){
     f.setAccessible(true);Map<?,?> map=(Map<?,?>)f.get(manager);
     for(var entry:map.entrySet()){
      Object value=entry.getValue();Method serial=Arrays.stream(value.getClass().getMethods()).filter(m->m.getName().equals("serializeNBT")&&m.getParameterCount()==1&&!m.isBridge()).findFirst().orElseThrow();
      values.put(f.getName()+"/"+entry.getKey(),String.valueOf(serial.invoke(value,new Object[]{server.registryAccess()})));
     }
    }
    configs.put(name,values);
   }
   result.put("configs",configs);
   int[] scans={0};
   ResourceManager counted=(ResourceManager)Proxy.newProxyInstance(ResourceManager.class.getClassLoader(),new Class[]{ResourceManager.class},(proxy,method,args)->{
    if(method.getName().equals("listResources"))scans[0]++;
    try{return method.invoke(resources,args);}catch(InvocationTargetException ex){throw ex.getCause();}
   });
   var sample=BuiltInRegistries.ITEM.stream().limit(16).toList();
   var utility=Class.forName("com.nukateam.ntgl.modules.datapack.ConfigUtils");
   Method load=Arrays.stream(utility.getMethods()).filter(m->m.getName().equals("getConfigMap")).findFirst().orElseThrow();
   var equipment=Class.forName("com.nukateam.chassis_core.common.config.EquipmentConfig");
   long start=System.nanoTime();
   Object loaded=load.invoke(null,counted,BuiltInRegistries.ITEM,(Predicate<Object>)sample::contains,equipment,"cc/equipment");
   result.put("sample_items",sample.size());result.put("physical_scans",scans[0]);result.put("sample_configs",((Map<?,?>)loaded).size());result.put("sample_ms",(System.nanoTime()-start)/1e6);
   boolean pass=scans[0]<=1;result.put("single_scan_pass",pass);
   Files.writeString(Path.of("ntgl-loading-result.json"),new GsonBuilder().setPrettyPrinting().create().toJson(result));
   System.out.println("NTGL_QA_RESULT scans="+scans[0]+" expected_max=1 pass="+pass);return pass?1:0;
  }catch(Throwable ex){ex.printStackTrace();System.out.println("NTGL_QA_ERROR "+ex);return 0;}
 }));
 e.getDispatcher().register(Commands.literal("ntglreload").requires(s->s.hasPermission(4)).executes(c->{
  var server=c.getSource().getServer();
  server.reloadResources(server.getPackRepository().getSelectedIds()).whenComplete((v,error)->{
   System.out.println(error==null?"NTGL_RELOAD_READY":"NTGL_RELOAD_ERROR "+error);
  });return 1;
 }));}
}
