package io.heavenland.mebot.paragon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BreedingPDA {

	@JsonProperty("PDA")
	private String pda;
	@JsonProperty("State")
	private String state;
	@JsonProperty("Creator")
	private String creator;

	@JsonProperty("NftMint1")
	private String nftMint1;
	@JsonProperty("NftMint2")
	private String nftMint2;
	@JsonProperty("BornMint")
	private String bornMint;

	@JsonProperty("EnterTime")
	private Long enterTime;
	@JsonProperty("UnlockTime")
	private Long unlockTime;
	@JsonProperty("DepositAmount")
	private Long depositAmount;

	@JsonProperty("Status")
	private Long status;

}
