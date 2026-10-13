package ntglloadqa;
import lastbastion.resources.ConfigScanResources;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ResourceScanTests {
 static int passed;
 static void check(boolean b,String message){if(!b)throw new AssertionError(message);passed++;}
 static ResourceLocation id(String value){return ResourceLocation.parse(value);}
 static class Source implements ResourceManager {
  final LinkedHashMap<ResourceLocation,Resource> files=new LinkedHashMap<>();
  int scans,gets,stackScans,stacks,packs;
  public Set<String> getNamespaces(){return Set.of("ntgl","addon");}
  public Optional<Resource> getResource(ResourceLocation id){gets++;return Optional.empty();}
  public List<Resource> getResourceStack(ResourceLocation id){stacks++;return List.of();}
  public Map<ResourceLocation,Resource> listResources(String root,Predicate<ResourceLocation> filter){
   scans++;var result=new LinkedHashMap<ResourceLocation,Resource>();
   files.forEach((id,r)->{if(id.getPath().startsWith(root+"/")&&filter.test(id))result.put(id,r);});return result;
  }
  public Map<ResourceLocation,List<Resource>> listResourceStacks(String root,Predicate<ResourceLocation> f){stackScans++;return Map.of();}
  public Stream<PackResources> listPacks(){packs++;return Stream.empty();}
 }
 public static void main(String[] args){
  Source source=new Source();
  for(String s:List.of("ntgl:cc/equipment/a.json","addon:cc/equipment/nested/a.json","ntgl:cc/equipment/b.json","ntgl:cc/equipment/readme.txt","ntgl:weapons/a.json"))source.files.put(id(s),null);
  ResourceManager first=new ConfigScanResources(source);
  var a=first.listResources("cc/equipment",p->p.getPath().endsWith("a.json"));
  check(a.size()==2,"suffix matching includes nested and other namespaces exactly as original");
  check(new ArrayList<>(a.keySet()).equals(List.of(id("ntgl:cc/equipment/a.json"),id("addon:cc/equipment/nested/a.json"))),"delegate iteration order retained for stable upstream sorting");
  a.clear();
  check(first.listResources("cc/equipment",p->p.getPath().endsWith("a.json")).size()==2,"caller map mutation cannot alter cached listing");
  check(first.listResources("cc/equipment",p->p.getPath().endsWith("b.json")).size()==1,"independent predicates");
  check(first.listResources("cc/equipment",p->true).size()==4,"no implicit JSON filter");
  check(source.scans==1,"one physical scan per directory per invocation");
  check(first.listResources("weapons",p->true).size()==1&&source.scans==2,"separate roots are not conflated");
  check(first.listResources("empty",p->true).isEmpty()&&first.listResources("empty",p->true).isEmpty()&&source.scans==3,"empty scan is cached");
  source.files.put(id("ntgl:cc/equipment/c.json"),null);
  ResourceManager reload=new ConfigScanResources(source);
  check(reload.listResources("cc/equipment",p->true).size()==5,"a new invocation sees changed datapacks");
  check(first.listResources("cc/equipment",p->true).size()==4,"in-progress invocation remains independent");
  Source other=new Source();check(new ConfigScanResources(other).listResources("cc/equipment",p->true).isEmpty(),"no state leak across worlds/managers");
  check(first.getNamespaces().equals(source.getNamespaces()),"namespaces delegated");
  check(first.getResource(id("ntgl:cc/equipment/a.json")).isEmpty()&&source.gets==1,"getResource uses live delegate for original pack priority");
  check(first.getResourceStack(id("ntgl:cc/equipment/a.json")).isEmpty()&&source.stacks==1,"resource stacks delegated");
  check(first.listResourceStacks("cc/equipment",p->true).isEmpty()&&source.stackScans==1,"stack listing delegated");
  try(var packs=first.listPacks()){check(packs.count()==0&&source.packs==1,"packs delegated");}
  System.out.println("RESOURCE_SCAN_TESTS pass="+passed+" fail=0");
 }
}
