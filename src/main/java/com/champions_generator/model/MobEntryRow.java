package com.champions_generator.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MobEntryRow {
    private final String mobId;
    private final int minRank;
    private final int maxRank;
    private final List<String> affixPresets;
    private final List<String> affixList;
    private final String affixListType;

    @Override
    public String toString() {
        return new StringBuilder()
                .append("[[entities]]\n")
                .append("  entity = \"").append(mobId).append("\"\n")
                .append("  minTier = ").append(minRank).append("\n")
                .append("  maxTier = ").append(maxRank).append("\n")
                .append("  presetAffixes = ").append(affixPresets).append("\n")
                .append("  affixList = ").append(affixList).append("\n")
                .append("  affixPermission = \"").append(affixListType).append("\"\n")
                .append("\n")
                .toString();
    }
}
