package io.heavenland.mebot.domain;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ListingStatus {
    LISTED, UNLISTED;

    @JsonValue
    public String jsonValue() {
        return name().toLowerCase();
    }
}
