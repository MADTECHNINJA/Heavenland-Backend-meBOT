package io.heavenland.mebot;

import org.p2p.solanaj.rpc.Cluster;

public class Constants {
    public static final String PRIVATE_MAINNET_RPC_SERVER = "https://weathered-proud-shape.solana-mainnet.quiknode.pro/75114e7c361deae9deac7f76ac11831fcfccd677/";
    public static final String PRIVATE_DEVNET_RPC_SERVER = "https://little-crimson-lake.solana-devnet.quiknode.pro/2724d6c13bb792a391ceb7ecb5ab72a52e41ffa2/";

    public static final boolean USE_MAINNET = true;
    public static final Cluster CLUSTER = USE_MAINNET ? Cluster.MAINNET : Cluster.DEVNET;
}
