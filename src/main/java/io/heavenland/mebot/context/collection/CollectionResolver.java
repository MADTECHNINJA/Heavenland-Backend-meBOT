package io.heavenland.mebot.context.collection;

import io.heavenland.common.nfts.alphas.model.AlphasOnChainModel;
import io.heavenland.common.nfts.loyalty.model.LoyaltyOnChainModel;
import io.heavenland.common.nfts.parcels.model.ParcelBucketOnChainModel;
import io.heavenland.common.nfts.parcels.model.ParcelOnChainModel;
import io.heavenland.mebot.Constants;
import io.heavenland.mebot.domain.NftCollection;

public class CollectionResolver {

    private static CollectionResolver instance;

    public static CollectionResolver instance() {
        if (instance == null) {
            instance = new CollectionResolver();
        }
        return instance;
    }

    private final ParcelOnChainModel parcelOnChainModel;
    private final ParcelBucketOnChainModel parcelBucketOnChainModel;
    private final LoyaltyOnChainModel loyaltyOnChainModel;
    private final AlphasOnChainModel alphasOnChainModel;

    private CollectionResolver() {
        parcelOnChainModel = null;//new ParcelOnChainModel(Constants.USE_MAINNET);
        parcelBucketOnChainModel = null;//new ParcelBucketOnChainModel(Constants.USE_MAINNET);
        loyaltyOnChainModel = new LoyaltyOnChainModel(Constants.USE_MAINNET);
        alphasOnChainModel = new AlphasOnChainModel(Constants.USE_MAINNET);
    }

    public NftCollection resolveCollection(String mintAddress) {
        if (alphasOnChainModel.isTokenAddressInCollection(mintAddress)) {
            return NftCollection.HL_ALPHAS;
        } else if (loyaltyOnChainModel.isTokenAddressInCollection(mintAddress)) {
            return NftCollection.HL_LOYALTY;
        }
        return null;
    }
}
