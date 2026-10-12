package lastbastion.client;

import java.nio.file.*;
import java.util.Properties;
import net.neoforged.fml.loading.FMLPaths;

/** Personal preference file deliberately excluded from Packwiz. */
public final class BastionPreferences {
    public boolean visible=true;
    public int position=0, size=0;
    private final Path file;
    public BastionPreferences() { this(FMLPaths.CONFIGDIR.get().resolve("lastbastion-hud-local.properties")); }
    public BastionPreferences(Path file) {
        this.file=file;
        if(!Files.isRegularFile(file))return;
        var p=new Properties();
        try(var stream=Files.newInputStream(file)) {
            p.load(stream);visible=Boolean.parseBoolean(p.getProperty("visible","true"));
            position=parse(p.getProperty("position"),0,3);size=parse(p.getProperty("size"),0,2);
        } catch(Exception error) { log(error); }
    }
    private static int parse(String value,int fallback,int max) {
        try {int n=Integer.parseInt(value);return n>=0&&n<=max?n:fallback;} catch(Exception e) {return fallback;}
    }
    public float scale() { return switch(size) {case 1->0.85F;case 2->1.15F;default->1.0F;}; }
    public void save() {
        var p=new Properties();p.setProperty("visible",Boolean.toString(visible));p.setProperty("position",Integer.toString(position));p.setProperty("size",Integer.toString(size));
        try {
            Files.createDirectories(file.getParent());var temp=file.resolveSibling(file.getFileName()+".tmp");
            try(var stream=Files.newOutputStream(temp)){p.store(stream,"LAST BASTION personal HUD preferences");}
            try{Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}
            catch(AtomicMoveNotSupportedException e){Files.move(temp,file,StandardCopyOption.REPLACE_EXISTING);}
        }catch(Exception error){log(error);}
    }
    private static void log(Exception error){org.slf4j.LoggerFactory.getLogger("LastBastionHUD").warn("Cannot read/write personal HUD preferences",error);}
}
