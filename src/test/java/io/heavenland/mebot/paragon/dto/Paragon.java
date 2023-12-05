package io.heavenland.mebot.paragon.dto;

import io.heavenland.mebot.domain.NftMetadata;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Data
@RequiredArgsConstructor
public class Paragon {

	@Getter
	@RequiredArgsConstructor
	public enum CoreAttribute {
		STAMINA("Stamina"),
		STAMINA_RECOVERY_RATE("Stamina Recovery Rate"),
		STAMINA_USAGE_DURING_SPRINT("Stamina Usage during Sprint"),
		STAMINA_USAGE_PER_TRANSLOCATION("Stamina Usage per Translocation"),

		WALK_SPEED("Walk Speed"),
		SPRINT_SPEED("Sprint Speed"),

		JUMP_HEIGHT("Jump Height"),

		MAX_TRANSLOCATION_DISTANCE("Max Translocation Distance"),
		TRANSLOCATION_RECOVERY("Translocation Recovery");

		private final String title;


	}

	private final String mint;

	private int tier;
	private int score;
	private int breedCount;
	private final Map<CoreAttribute, Integer> attValues = new HashMap<>();

	public static Paragon fromMetadata(NftMetadata metaData) {
		if (metaData == null) {
			return null;
		}
		Paragon paragon = new Paragon(metaData.getMintAddress());
		paragon.setTier(Integer.parseInt(metaData.getAttributes().get("Tier")));
		paragon.setScore(Integer.parseInt(metaData.getAttributes().get("Score")));
		paragon.setBreedCount(Integer.parseInt(metaData.getAttributes().get("Breed Count")));
		for (CoreAttribute coreAttribute : CoreAttribute.values()) {
			paragon.getAttValues().put(
					coreAttribute,
					Integer.parseInt(metaData.getAttributes().get(coreAttribute.getTitle()))
			);
		}
		return paragon;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Paragon paragon = (Paragon) o;
		return Objects.equals(mint, paragon.mint);
	}

	@Override
	public int hashCode() {
		return Objects.hash(mint);
	}

}
