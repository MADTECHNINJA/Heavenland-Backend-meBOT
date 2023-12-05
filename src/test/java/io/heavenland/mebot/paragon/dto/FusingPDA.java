package io.heavenland.mebot.paragon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FusingPDA {

	@JsonProperty("PDA")
	private String pda;
	@JsonProperty("State")
	private String state;
	@JsonProperty("Creator")
	private String creator;

	@JsonProperty("NftMint")
	private String nftMint;
	@JsonProperty("NftMint1")
	private String nftMint1;
	@JsonProperty("NftMint2")
	private String nftMint2;
	@JsonProperty("NftMint3")
	private String nftMint3;
	@JsonProperty("NftMint4")
	private String nftMint4;
	@JsonProperty("Fusion")
	private String fusion;

	@JsonProperty("EnterTime")
	private Long enterTime;
	@JsonProperty("UnlockTime")
	private Long unlockTime;
	@JsonProperty("DepositAmount")
	private Long depositAmount;

	@JsonProperty("Status")
	private Long status;

}
