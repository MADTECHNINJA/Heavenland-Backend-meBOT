package io.heavenland.mebot.context.collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.p2p.solanaj.rpc.Cluster;

import java.io.IOException;

public class MetadataProviderTest {

    @Test
    public void testOnChain() throws IOException {
        var provider = MetadataProvider.instance(Cluster.MAINNET);
        var meta = provider.getOnChainMetadata("AgwWqXGaKaDgYquCHud7hfmMh9W6PyueVSUwHPuy1azv");
        Assertions.assertEquals("Heaven Land #2379", meta.getData().getName());
    }

    @Test
    public void testOffChain() throws IOException {
        var provider = MetadataProvider.instance(Cluster.MAINNET);
        var meta = provider.getOffChainMetadata("AgwWqXGaKaDgYquCHud7hfmMh9W6PyueVSUwHPuy1azv");
        Assertions.assertEquals("Downtown 2A [27, -362]", meta.getName());
    }
}
