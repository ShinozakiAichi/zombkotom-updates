package lastbastion.hive.routing;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Path;
import java.util.List;
/** Transient route to a fixed observation. Never stores a live target-position supplier. */
public record RouteAssignment(BlockPos objective,long expires,RouteGraph.Route route,List<Path> paths){
 public RouteAssignment {objective=objective.immutable();paths=paths.stream().map(Path::copy).toList();}
 @Override public List<Path> paths(){return paths.stream().map(Path::copy).toList();}
}
