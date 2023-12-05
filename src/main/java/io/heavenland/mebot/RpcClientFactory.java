package io.heavenland.mebot;

import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;

public class RpcClientFactory {

    private static final boolean USE_PRIVATE_ENDPOINTS = true; // change to false touse public RPCs

    public static RpcClient create(Cluster cluster) {
        if (USE_PRIVATE_ENDPOINTS) {
            return create(cluster == Cluster.MAINNET ? Constants.PRIVATE_MAINNET_RPC_SERVER : Constants.PRIVATE_DEVNET_RPC_SERVER);
        } else {
            return create(cluster.getEndpoint());
        }
    }

    public static RpcClient create(String endpointUrl) {
        return new RpcClient(endpointUrl);
    }
}
