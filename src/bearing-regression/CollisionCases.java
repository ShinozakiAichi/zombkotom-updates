package bearingqa;
import com.simibubi.create.foundation.collision.*;
import net.minecraft.world.phys.Vec3;
import java.nio.file.*;
import java.util.*;

public final class CollisionCases {
    public static List<String> run() {
        var rows=new ArrayList<String>();
        for(int angle:new int[]{0,15,45,90,135,180,270})
        for(double offset:new double[]{0,0.001,0.2,0.7,1.2,3})
        for(double motion:new double[]{0,0.15,-0.15}) {
            String id=angle+":"+offset+":"+motion;
            var list=new CollisionList();list.size=1;
            list.extentsX[0]=list.extentsY[0]=list.extentsZ[0]=0.5;
            var box=new OrientedBB(new Vec3(offset,0,0),new Vec3(.3,.9,.3),new Matrix3d().asYRotation((float)Math.toRadians(angle)));
            try {
                var r=ContinuousOBBCollider.collideMany(list,new CollisionList(),box,new Vec3(motion,0,0),0.6f,false);
                boolean finite=finite(r.collisionResponse)&&finite(r.normal)&&finite(r.location)&&Double.isFinite(r.temporalResponse);
                rows.add(id+"|"+(finite?"OK":"NONFINITE")+"|"+r.collisionResponse+"|"+r.normal+"|"+r.location+"|"+r.temporalResponse+"|"+r.surfaceCollision);
            } catch(Throwable ex) { rows.add(id+"|EXCEPTION|"+ex.getClass().getSimpleName()+"|"+ex.getMessage()); }
        }
        return rows;
    }
    private static boolean finite(Vec3 v){return v!=null&&Double.isFinite(v.x)&&Double.isFinite(v.y)&&Double.isFinite(v.z);}
    public static void main(String[] args)throws Exception {var rows=run();Files.write(Path.of(args[0]),rows);System.out.println("cases="+rows.size()+" exceptions="+rows.stream().filter(s->s.contains("|EXCEPTION|")).count());}
}
