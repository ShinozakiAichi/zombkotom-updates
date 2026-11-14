package lastbastion.hive;
import java.util.function.BooleanSupplier;
/** Bounded server-tick sequence. A failed authorization/release cancels the remainder. */
public final class BurstSequence {
 private int remaining;
 private long next,deadline,last;
 public BurstSequence(int remaining,long now){
  if(remaining<1||remaining>4||now<0)throw new IllegalArgumentException();
  this.remaining=remaining;next=now+10;deadline=now+80;last=now;
 }
 public boolean advance(long now,BooleanSupplier release){
  if(remaining==0)return false;
  if(now<last||now>deadline){remaining=0;return false;}
  last=now;if(now<next)return true;
  if(!release.getAsBoolean()){remaining=0;return false;}
  remaining--;next=now+10;return remaining>0;
 }
}
