package io.heavenland.mebot.clients.magiceden.dto;

import lombok.Data;

import java.util.List;

@Data
public class MagicEdenTxDTO {

	private String type;
	private List<Integer> data;

}
