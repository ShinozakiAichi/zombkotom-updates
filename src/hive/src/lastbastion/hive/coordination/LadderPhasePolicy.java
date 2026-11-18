package lastbastion.hive.coordination;
/** Assembly can prepare an attack; leaving the apex belongs to the assault only. */
public final class LadderPhasePolicy {
 private LadderPhasePolicy(){}
 public static boolean assemble(EncounterState.Phase group,SiegeClock.Phase siege){
  return (group==EncounterState.Phase.GATHER||group==EncounterState.Phase.POSITION||group==EncounterState.Phase.ASSAULT)
   &&(siege==SiegeClock.Phase.PREPARE||siege==SiegeClock.Phase.ASSAULT);
 }
 public static boolean cross(EncounterState.Phase group,SiegeClock.Phase siege){
  return group==EncounterState.Phase.ASSAULT&&siege==SiegeClock.Phase.ASSAULT;
 }
}
