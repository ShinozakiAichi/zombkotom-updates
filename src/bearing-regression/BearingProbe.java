package bearingqa;
import java.nio.file.*;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.minecraft.commands.Commands;

@Mod("bearing_qa")
public class BearingProbe {
    public BearingProbe(){NeoForge.EVENT_BUS.addListener(this::register);}
    private void register(RegisterCommandsEvent e){e.getDispatcher().register(Commands.literal("bearingqa").requires(s->s.hasPermission(4)).executes(c->{
        try {
            var rows=CollisionCases.run();Files.write(Path.of("bearing-qa-results.txt"),rows);
            var baseline=Files.readAllLines(Path.of("bearing-qa-baseline.txt"));
            int regression=0,unchanged=0,failed=0;
            for(int i=0;i<rows.size();i++){
                String old=baseline.get(i),now=rows.get(i);
                if(old.contains("|EXCEPTION|")){if(now.contains("|OK|"))regression++;else failed++;}
                else if(old.equals(now))unchanged++;else failed++;
            }
            System.out.println("BEARING_QA_RESULT cases="+rows.size()+" recovered="+regression+" unchanged="+unchanged+" failed="+failed);
        }catch(Throwable ex){ex.printStackTrace();System.out.println("BEARING_QA_FAIL "+ex);}
        return 1;
    }));}
}
