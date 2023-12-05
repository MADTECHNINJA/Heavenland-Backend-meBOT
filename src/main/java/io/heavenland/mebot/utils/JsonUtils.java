package io.heavenland.mebot.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtils {

	private JsonUtils() {
	}

	private static ObjectMapper mapper;

	private static ObjectMapper getMapper() {
		if (mapper == null) {
			mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		}
		return mapper;
	}

	public static String objectToString(Object object) {
		try {
			return getMapper().writeValueAsString(object);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	public static String objectToPrettyString(Object object) {
		try {
			return getMapper().writerWithDefaultPrettyPrinter()
					.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	public static <T> T stringToObject(String s, Class<T> clazz) {
		try {
			return getMapper().readValue(s, clazz);
		} catch (IOException e) {
			return null;
		}
	}

	public static <T> T stringToObject(String s, TypeReference<T> type) {
		try {
			return getMapper().readValue(s, type);
		} catch (IOException e) {
			return null;
		}

	}

}
