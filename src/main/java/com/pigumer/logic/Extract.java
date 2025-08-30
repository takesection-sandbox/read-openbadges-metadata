package com.pigumer.logic;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class Extract {

    private boolean filter(Map<String, String> text) {
        String keyword = text.get("keyword");
        return "openbadges".equals(keyword);
    }

    public Map<String, String> extract(Collection<Map<String, String>> text) {
        Optional<Map<String, String>> maybeOpenbadges = text.stream().filter(this::filter).findFirst();
        return maybeOpenbadges.orElse(null);
    }
}
