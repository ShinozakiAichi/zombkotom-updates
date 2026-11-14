package lastbastion.hive;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import java.util.ArrayList;
import java.util.Comparator;

/** Creates bounded groups from a directly observed elevated target, not a hidden-player scan. */
public final class FormationTrigger {
 private FormationTrigger(){}
 @SubscribeEvent
 public static void consider(EntityTickEvent.Pre event){
  if(!(event.getEntity() instanceof Zombie zombie)||!(zombie.level() instanceof ServerLevel level)
   ||Math.floorMod(level.getGameTime()+zombie.getId(),80)!=0||!zombie.onGround()||zombie.isNoAi()
   ||LivingFormation.assignment(zombie)!=null)return;
  int maxWidth=PyramidPlan.widthForNight(Math.max(1,level.getDayTime()/24000+1));if(maxWidth==0)return;
  var target=zombie.getTarget();if(target==null||!target.isAlive()||!zombie.getSensing().hasLineOfSight(target))return;
  double rise=target.getY()-zombie.getY();if(rise<2.5||rise>maxWidth*1.95+1||zombie.distanceToSqr(target)>144)return;
  if(!PatrolPathBudget.reserve(level))return;
  Vec3 forward=target.position().subtract(zombie.position()).multiply(1,0,1);
  if(forward.lengthSqr()<0.25)return;forward=forward.normalize();
  Vec3 origin=new Vec3(target.getX()-forward.x*1.2,zombie.getY(),target.getZ()-forward.z*1.2);
  if(!level.getEntitiesOfClass(Zombie.class,new AABB(origin,origin).inflate(3,10,3),z->LivingFormation.assignment(z)!=null).isEmpty())return;
  var candidates=new ArrayList<>(level.getEntitiesOfClass(Zombie.class,new AABB(origin,origin).inflate(8,2,8),
   z->z.isAlive()&&!z.isBaby()&&!z.isNoAi()&&z.onGround()&&Math.abs(z.getBbHeight()-1.95)<0.1&&LivingFormation.assignment(z)==null));
  int width=Math.min(maxWidth,Math.max(2,(int)Math.ceil(rise/1.95)));
  while(width>=2&&candidates.size()<width*(width+1)/2)width--;
  if(width<2)return;
  var slots=PyramidPlan.create(width,origin.x,origin.y,origin.z,forward.x,forward.z);
  for(var slot:slots){
   var pos=BlockPos.containing(slot.x(),slot.y(),slot.z());
   if(!level.hasChunkAt(pos))return;
   var body=new AABB(slot.x()-0.3,slot.y(),slot.z()-0.3,slot.x()+0.3,slot.y()+1.95,slot.z()+0.3);
   if(level.getBlockCollisions(zombie,body).iterator().hasNext())return;
   if(slot.tier()==0){var floor=pos.below();if(!level.getBlockState(floor).isFaceSturdy(level,floor,Direction.UP))return;}
  }
  var members=new ArrayList<Zombie>();
  for(var slot:slots){
   candidates.sort(Comparator.comparingDouble(z->z.distanceToSqr(slot.x(),origin.y,slot.z())));
   members.add(candidates.remove(0));
  }
  LivingFormation.form(level,members,slots);
 }
}
