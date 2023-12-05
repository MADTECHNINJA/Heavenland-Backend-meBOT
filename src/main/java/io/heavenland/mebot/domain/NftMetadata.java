package io.heavenland.mebot.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class NftMetadata {

	private String mintAddress;
	private String updateAuthority;
	private String name;
	private Map<String, String> attributes = new HashMap<>();

}
