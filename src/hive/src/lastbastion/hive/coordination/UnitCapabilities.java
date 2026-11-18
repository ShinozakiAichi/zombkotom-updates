package lastbastion.hive.coordination;
import java.util.Set;
/** Species adapters opt into roles explicitly. Defining an adapter does not install any AI. */
public record UnitCapabilities(Set<Ability> abilities) {
 public enum Ability {COORDINATE,SCOUT,PRESSURE,FLANK,EXIT_WATCH,LADDER}
 public interface Adapter<T> {UnitCapabilities describe(T unit);}
 public static final UnitCapabilities NONE=new UnitCapabilities(Set.of());
 public UnitCapabilities {
  abilities=Set.copyOf(abilities);
  if(!abilities.isEmpty()&&!abilities.contains(Ability.COORDINATE))throw new IllegalArgumentException("Roles require coordination capability");
 }
 public boolean supports(Ability ability){return abilities.contains(ability);}
}
