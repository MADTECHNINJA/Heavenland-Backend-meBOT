package io.heavenland.mebot.paragon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StampingPDA {

	@JsonProperty("PDA")
	private String pda;
	@JsonProperty("State")
	private String state;
	@JsonProperty("Creator")
	private String creator;

	@JsonProperty("Paragon")
	private String paragon;
	@JsonProperty("Avatar")
	private String avatar;

	@JsonProperty("EnterTime")
	private Long enterTime;
	@JsonProperty("DepositAmount")
	private Long depositAmount;
	@JsonProperty("Verified")
	private Long verified;

}
