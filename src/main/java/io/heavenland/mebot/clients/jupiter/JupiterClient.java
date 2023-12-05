package io.heavenland.mebot.clients.jupiter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.heavenland.mebot.clients.jupiter.dto.JupiterResponseDTO;
import io.heavenland.mebot.clients.jupiter.dto.JupiterRouteDTO;
import io.heavenland.mebot.clients.jupiter.dto.JupiterSwapDTO;
import io.heavenland.mebot.clients.jupiter.dto.JupiterTransactionsDTO;
import io.heavenland.mebot.domain.Account;
import io.heavenland.mebot.domain.Token;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Slf4j
public class JupiterClient {

	private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

	private static JupiterClient instance;
	private static final String API_BASE_URL = "https://quote-api.jup.ag";

	public static JupiterClient instance() {
		if (instance == null) {
			instance = new JupiterClient();
		}
		return instance;
	}

	protected Invocation.Builder getRequestBuilder(UriBuilder uri) {
		Client client = ClientBuilder.newClient();
		return client.target(uri).request();
	}

	public List<JupiterRouteDTO> getQuote(Token in, Token out, BigInteger amount, BigDecimal slippage, boolean onlyDirectRoutes) {
		UriBuilder uri = UriBuilder
				.fromUri(API_BASE_URL + "/v1/quote")
				.queryParam("inputMint", in.getAddress())
				.queryParam("outputMint", out.getAddress())
				.queryParam("amount", amount)
				.queryParam("slippage", slippage)
				.queryParam("onlyDirectRoutes", onlyDirectRoutes);

		String response = getRequestBuilder(uri).get(String.class);

		JupiterResponseDTO<List<JupiterRouteDTO>> typedResponse;
		try {
			typedResponse = mapper.readValue(response, new TypeReference<>() {
			});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not deserialize JSON", e);
		}
		return typedResponse.getData();
	}

	public JupiterTransactionsDTO swap(Account account, JupiterRouteDTO route, boolean wrapUnwrapSol) {
		UriBuilder uri = UriBuilder
				.fromUri(API_BASE_URL + "/v1/swap");

		JupiterSwapDTO swapDTO = new JupiterSwapDTO();
		swapDTO.setRoute(route);
		swapDTO.setUserPublicKey(account.getAddress());
		swapDTO.setWrapUnwrapSOL(wrapUnwrapSol);

		String response;
		try {
			response = getRequestBuilder(uri).post(Entity.entity(swapDTO, MediaType.APPLICATION_JSON_TYPE), String.class);
		} catch (InternalServerErrorException e) {
			log.error("Jupiter Error: {}", e.getMessage());
			return null;
		}

		JupiterTransactionsDTO typedResponse;
		try {
			typedResponse = mapper.readValue(response, new TypeReference<>() {
			});
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not deserialize JSON", e);
		}
		return typedResponse;
	}

}
