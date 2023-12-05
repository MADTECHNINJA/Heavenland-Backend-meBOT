package io.heavenland.mebot.paragon.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class BreedStats {

	private final int inputMaxScore;
	private final Map<Paragon.CoreAttribute, Integer> inputMaxAttValue = new HashMap<>();

	private final double outputExpectedScore;

	public double getAbsIncrease() {
		return outputExpectedScore - inputMaxScore;
	}

	public double getIncreasePercent() {
		return 100 * (outputExpectedScore - inputMaxScore) / inputMaxScore;
	}

}
