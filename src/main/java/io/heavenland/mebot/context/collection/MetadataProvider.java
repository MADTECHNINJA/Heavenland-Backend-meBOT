package io.heavenland.mebot.context.collection;

import io.heavenland.tools.metaplex.MetadataLoader;
import io.heavenland.tools.metaplex.dto.Metadata;
import io.heavenland.tools.metaplex.dto.OffChainMetadata;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.Cluster;

import java.util.LinkedHashMap;
import java.util.Map;

public class MetadataProvider {

    private static MetadataProvider instance;

    private static final Integer MAX_SIZE = 500;

    private Cluster cluster;
    private MetadataLoader metadataLoader;
    private LinkedHashMap<String, Metadata> cacheOnChain;
    private LinkedHashMap<String, OffChainMetadata> cacheOffChain;


    public static MetadataProvider instance(Cluster cluster) {
        if (instance == null || instance.cluster != cluster) {
            instance = new MetadataProvider(cluster);
        }
        return instance;
    }

    public MetadataProvider(Cluster cluster) {
        cacheOnChain = new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Metadata> eldest) {
                return size() > MAX_SIZE;
            }
        };

        cacheOffChain = new LinkedHashMap<>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, OffChainMetadata> eldest) {
                return size() > MAX_SIZE;
            }
        };

        metadataLoader = new MetadataLoader(cluster);
    }

    public Metadata getOnChainMetadata(String address) {
        if (!cacheOnChain.containsKey(address)) {
            var meta = metadataLoader.getOnChainMetadata(new PublicKey(address));
            cacheOnChain.put(address, meta);
        }
        return cacheOnChain.get(address);
    }

    public OffChainMetadata getOffChainMetadata(String address) {
        if (!cacheOffChain.containsKey(address)) {
            var meta = metadataLoader.getOffChainMetadata(new PublicKey(address));
            cacheOffChain.put(address, meta);
        }
        return cacheOffChain.get(address);
    }
}
