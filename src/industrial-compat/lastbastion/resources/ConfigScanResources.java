package lastbastion.resources;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

/** One ConfigUtils invocation owns one instance; no reload/world-global cache. */
public final class ConfigScanResources implements ResourceManager {
    private final ResourceManager delegate;
    private final Map<String, Map<ResourceLocation, Resource>> listings = new HashMap<>();

    public ConfigScanResources(ResourceManager delegate) { this.delegate = delegate; }

    @Override
    public Map<ResourceLocation, Resource> listResources(String root, Predicate<ResourceLocation> filter) {
        Map<ResourceLocation, Resource> all = listings.computeIfAbsent(root,
                path -> delegate.listResources(path, id -> true));
        Map<ResourceLocation, Resource> selected = new LinkedHashMap<>();
        // Preserve iteration order: NTGL sorts same-namespace matches stably.
        all.forEach((id, resource) -> { if (filter.test(id)) selected.put(id, resource); });
        return selected;
    }

    @Override public Set<String> getNamespaces() { return delegate.getNamespaces(); }
    @Override public Optional<Resource> getResource(ResourceLocation id) { return delegate.getResource(id); }
    @Override public List<Resource> getResourceStack(ResourceLocation id) { return delegate.getResourceStack(id); }
    @Override public Map<ResourceLocation, List<Resource>> listResourceStacks(String root, Predicate<ResourceLocation> filter) {
        return delegate.listResourceStacks(root, filter);
    }
    @Override public Stream<PackResources> listPacks() { return delegate.listPacks(); }
}
