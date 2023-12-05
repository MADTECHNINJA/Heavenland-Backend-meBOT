package io.heavenland.mebot.token_sender;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VPassParser {

	public static void main(String[] args) throws IOException {
		List<String> lines = FileUtils.readLines(new File("mebot"+File.separator+"doggo" + File.separator + "vpass1-tmp.csv"));
		lines = lines.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
				.entrySet().stream().map(e -> e.getKey() + ","+e.getValue()).collect(Collectors.toList());
		FileUtils.writeLines(new File("mebot"+File.separator+"doggo" + File.separator + "vpass1.csv"), lines);
	}

}
