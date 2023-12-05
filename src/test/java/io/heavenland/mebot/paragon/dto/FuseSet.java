package io.heavenland.mebot.paragon.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Slf4j
public class FuseSet {

	private final Paragon mainParagon;
	private final List<Paragon> subParagons;
	private int resultingSore;

	public int getCostHto() {
		if (mainParagon.getTier() < 0 || mainParagon.getTier() > 4) {
			log.error("tier out of [0,4] range: {}", mainParagon.getTier());
		}
		return switch (mainParagon.getTier()) {
			case 0 -> 20;
			case 1 -> 40;
			case 2 -> 80;
			case 3 -> 160;
			case 4 -> 320;
			default -> 500;
		};
	}

	@Override
	public String toString() {
		return String.format("[%d + %s] -> %d, %d HTO",
				mainParagon.getScore(),
				subParagons.stream().map(p -> Integer.toString(p.getScore())).collect(Collectors.joining(",")),
				resultingSore, getCostHto());
	}

}
