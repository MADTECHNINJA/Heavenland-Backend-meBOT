package io.heavenland.mebot.paragon.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
@Slf4j
public class BreedPair {

	private final Paragon par1;
	private final Paragon par2;
	private BreedStats breedStats;

	public int getBreedCount() {
		return Math.max(par1.getBreedCount(), par2.getBreedCount());
	}

	public int getCostHto() {
		int breedCount = getBreedCount();
		if (breedCount < 0 || breedCount > 4) {
			log.error("breedCount out of [0,4] range: {}", breedCount);
		}
		return 10 * (breedCount + 1);
	}

	@Override
	public String toString() {
		return String.format("[%d,%d] -> %.2f [+%.2f%%, %d HTO]",
				par1.getScore(), par2.getScore(),
				breedStats.getOutputExpectedScore(),
				breedStats.getIncreasePercent(),
				getCostHto());
	}

}
