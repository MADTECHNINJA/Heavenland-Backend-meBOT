package io.heavenland.mebot.clients.jupiter.dto;

import lombok.Data;

@Data
public class JupiterResponseDTO<T> {

	private T data;
	private long timeTaken;

}
