package io.heavenland.mebot.clients.magiceden.dto;

import lombok.Data;

@Data
public class MagicEdenTxResponseDTO {

	private MagicEdenTxDTO tx;
	private MagicEdenTxDTO txSigned;

}
