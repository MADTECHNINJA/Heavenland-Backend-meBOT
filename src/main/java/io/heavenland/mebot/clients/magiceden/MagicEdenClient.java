package io.heavenland.mebot.clients.magiceden;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.heavenland.mebot.SecretProvider;
import io.heavenland.mebot.clients.magiceden.dto.MagicEdenListingDTO;
import io.heavenland.mebot.clients.magiceden.dto.MagicEdenTxResponseDTO;
import io.heavenland.mebot.clients.magiceden.dto.MagicEdenWalletTokenDTO;
import io.heavenland.mebot.domain.NftCollection;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.UriBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MagicEdenClient {

	public final String apiKey;

	private static final String API_BASE_URL = "https://api-mainnet.magiceden.dev/v2";
	private final ObjectMapper mapper = new ObjectMapper();

	private static MagicEdenClient instance;

    public static MagicEdenClient instance() {
        if (instance == null) {
            instance = new MagicEdenClient();
        }
        return instance;
    }

    private MagicEdenClient() {
        this.apiKey = SecretProvider.instance().getMagicEdenAPIKey();
    }


    private Long now;
    private int qps = 0;
    private static final int QPS_LIMIT = 60;

    private void callOrWait() {
        if (now == null) {
            now = Instant.now().getEpochSecond();
        }

        var newNow = Instant.now().getEpochSecond();
        //System.out.println("now = " + now + ", new now = " + newNow);
        if (now == newNow) {
            qps++;
           // System.out.println("qps++");
        } else {
            now = newNow;
            qps = 0;
         //   System.out.println("qps = 0");
        }


        if (qps >= QPS_LIMIT) {
            try {
                System.out.println("Waiting, too much QPS..");
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected Invocation.Builder getRequestBuilder(UriBuilder uri) {
        var client = ClientBuilder.newClient();
        var builder = client
                .target(uri)
                .request();

        if (apiKey != null) {
            builder.header("Authorization", "Bearer " + apiKey);
        }
        return builder;
    }

    public List<MagicEdenListingDTO> getListingsPage(NftCollection collection, Integer limit, Integer offset) {

        callOrWait();

        var uri = UriBuilder
                .fromUri(API_BASE_URL + "/collections/" + collection.getSymbol() + "/listings")
                .queryParam("limit", limit)
                .queryParam("offset", offset);

		var response = getRequestBuilder(uri).get(String.class);

		try {
			return mapper.readValue(response, new TypeReference<>() {
			});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not deserialize JSON", e);
		}
	}

    public List<MagicEdenWalletTokenDTO> getWalletTokensPage(String walletAddress, Integer limit, Integer offset) {

        callOrWait();

        var uri = UriBuilder
                .fromUri(API_BASE_URL + "/wallets/" + walletAddress + "/tokens")
                .queryParam("limit", limit)
                .queryParam("offset", offset);

		var response = getRequestBuilder(uri).get(String.class);

		try {
			return mapper.readValue(response, new TypeReference<>() {
			});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not deserialize JSON", e);
		}
	}

	// https://api.magiceden.dev/#51aff214-f517-47f6-b27b-66ca49029dbd
	public MagicEdenTxResponseDTO buyNow(String buyerAddress,
										 String sellerAddress,
										 String ahAddress,
										 String tokenMint,
										 String tokenAtaAddress,
										 BigDecimal price) {
		UriBuilder uri = UriBuilder
				.fromUri(API_BASE_URL + "/instructions/buy_now")
				.queryParam("buyer", buyerAddress)
				.queryParam("seller", sellerAddress)
				.queryParam("auctionHouseAddress", ahAddress)
				.queryParam("tokenMint", tokenMint)
				.queryParam("tokenATA", tokenAtaAddress)
				.queryParam("price", price)
				.queryParam("sellerExpiry", -1);
		String response = getRequestBuilder(uri).get(String.class);

		try {
			return mapper.readValue(response, new TypeReference<>() {
			});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not deserialize JSON", e);
		}
	}

	// https://api.magiceden.dev/#3bea2772-c43f-4273-8c0d-a6d4320c90c1
	public MagicEdenTxResponseDTO sell(String sellerAddress,
									   String ahAddress,
									   String tokenMint,
									   String tokenAccount,
									   BigDecimal price) {
		UriBuilder uri = UriBuilder
				.fromUri(API_BASE_URL + "/instructions/sell")
				.queryParam("seller", sellerAddress)
				.queryParam("auctionHouseAddress", ahAddress)
				.queryParam("tokenMint", tokenMint)
				.queryParam("tokenAccount", tokenAccount)
				.queryParam("price", price)
				.queryParam("expiry", -1);
		String response = getRequestBuilder(uri).get(String.class);

		try {
			return mapper.readValue(response, new TypeReference<>() {
			});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not deserialize JSON", e);
		}
	}


	//

	public List<MagicEdenListingDTO> getListings(NftCollection collection) {
		return getAll((limit, offset) -> getListingsPage(collection, limit, offset), 20);
	}

	public List<MagicEdenWalletTokenDTO> getWalletTokens(String walletAddress) {
		return getAll((limit, offset) -> getWalletTokensPage(walletAddress, limit, offset), 100);
	}

	//

    private <T> List<T> getAll(SingleAPIEndpointCallable<T> callable, Integer limit) {
        var all = new ArrayList<T>();

		var offset = 0;
		do {
			var result = callable.call(limit, offset);
			if (result.size() == 0) {
				break;
			}
			all.addAll(result);
			offset += limit;
		} while (true);

		return all;
	}


	//

	private interface SingleAPIEndpointCallable<T> {
		Collection<T> call(Integer limit, Integer offset);
	}

}
