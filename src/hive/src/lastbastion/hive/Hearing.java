package lastbastion.hive;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.ToDoubleFunction;

/** Server-side scoring model. Caller supplies same-dimension observations and measured occlusion. */
public final class Hearing {
    private Hearing() {}
    public record Position(double x, double y, double z) {
        public Position {
            if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z))
                throw new IllegalArgumentException("Nonfinite position");
        }
        double distanceSquared(Position other) {
            return Math.pow(x-other.x,2)+Math.pow(y-other.y,2)+Math.pow(z-other.z,2);
        }
    }
    public record Noise(UUID source, Position position, long tick, double strength) {
        public Noise {
            java.util.Objects.requireNonNull(source);
            java.util.Objects.requireNonNull(position);
            if (!Double.isFinite(strength) || strength <= 0) throw new IllegalArgumentException("Invalid strength");
        }
    }
    public record Heard(Noise noise, double score) {}
    public record Settings(long lifetimeTicks, double range, double minimumScore, double switchRatio) {
        public Settings {
            if (lifetimeTicks <= 0 || !Double.isFinite(range) || range <= 0
                || !Double.isFinite(minimumScore) || minimumScore <= 0
                || !Double.isFinite(switchRatio) || switchRatio < 1)
                throw new IllegalArgumentException("Invalid hearing settings");
        }
    }
    public static double score(Noise noise, Position listener, long now, double transmission, Settings settings) {
        if (now < noise.tick || !Double.isFinite(transmission) || transmission <= 0) return 0;
        double age=(double)now-(double)noise.tick;
        if (age >= settings.lifetimeTicks) return 0;
        double distance=listener.distanceSquared(noise.position);
        if (distance > settings.range*settings.range) return 0;
        double freshness=1-age/settings.lifetimeTicks;
        return noise.strength*Math.min(1,transmission)*freshness/(1+distance);
    }
    public static Optional<Heard> choose(Collection<Noise> noises, Position listener, long now,
                                          UUID currentSource, ToDoubleFunction<Noise> transmission, Settings settings) {
        Heard best=null, current=null;
        // Unoccluded volume is an upper bound. Expensive world queries are only
        // needed while a sound can still win (or supply hysteresis evidence).
        var ordered=noises.stream().filter(n->score(n,listener,now,1,settings)>=settings.minimumScore)
            .sorted(java.util.Comparator.<Noise>comparingDouble(n->score(n,listener,now,1,settings)).reversed()).toList();
        for (Noise noise:ordered) {
            double upper=score(noise,listener,now,1,settings);
            boolean incumbent=noise.source.equals(currentSource);
            if(best!=null && upper<best.score && (!incumbent || (current!=null && upper<=current.score))) continue;
            double value=score(noise,listener,now,transmission.applyAsDouble(noise),settings);
            if (value < settings.minimumScore) continue;
            Heard heard=new Heard(noise,value);
            if (best==null || value>best.score || (value==best.score && noise.source.compareTo(best.noise.source)<0)) best=heard;
            if (noise.source.equals(currentSource) && (current==null || value>current.score)) current=heard;
        }
        if (best==null) return Optional.empty();
        if (current!=null && !current.noise.source.equals(best.noise.source)
            && best.score < current.score*settings.switchRatio) return Optional.of(current);
        return Optional.of(best);
    }
}
