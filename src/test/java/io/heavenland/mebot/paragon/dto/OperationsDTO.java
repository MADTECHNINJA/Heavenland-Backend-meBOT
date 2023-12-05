package io.heavenland.mebot.paragon.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class OperationsDTO {

	private List<BreedingPDA> breeding = new ArrayList<>();
	private List<FusingPDA> fusion = new ArrayList<>();
	private List<StampingPDA> stamping = new ArrayList<>();

}
