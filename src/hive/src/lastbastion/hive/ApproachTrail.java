package lastbastion.hive;

import java.util.ArrayDeque;
import java.util.List;
import java.util.UUID;

/** A short personal trail becomes shared evidence only after reaching a visible player. */
public final class ApproachTrail {
    public record Point(int x, int y, int z) {
        double distanceSquared(Point other) {
            return Math.pow((double)x-other.x,2)+Math.pow((double)y-other.y,2)+Math.pow((double)z-other.z,2);
        }
    }
    private final ArrayDeque<Point> points = new ArrayDeque<>();
    private UUID target;
    private Point previous;
    private long sampledAt, startedAt, nextSuccess;
    public void reset() { target=null; previous=null; points.clear(); }
    public List<Point> observe(UUID observedTarget, Point current, double targetDistanceSquared, long now) {
        if (observedTarget==null || now<0 || !Double.isFinite(targetDistanceSquared) || targetDistanceSquared<0) {
            reset(); return List.of();
        }
        if (!observedTarget.equals(target) || now<sampledAt || now-sampledAt>100
                || now-startedAt>200 || previous!=null && previous.distanceSquared(current)>64) reset();
        if (target==null) {target=observedTarget;startedAt=now;}
        previous=current;sampledAt=now;
        if (now<nextSuccess) {points.clear();return List.of();}
        if (points.isEmpty() || points.getLast().distanceSquared(current)>=4) {
            points.addLast(current);
            if(points.size()>8)points.removeFirst();
        }
        if(targetDistanceSquared>9 || points.size()<3 || points.getFirst().distanceSquared(current)<36)return List.of();
        var result=List.copyOf(points);
        points.clear();nextSuccess=now+200;
        return result;
    }
}
