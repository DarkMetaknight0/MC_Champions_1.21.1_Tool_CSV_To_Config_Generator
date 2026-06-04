package com.champions_generator.service;

import com.champions_generator.model.AllowChampions;
import com.champions_generator.model.MobEntryRow;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.commons.lang3.StringUtils;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class CsvReaderService {

    public static final String CSV_PATH_MOB = "Champions Damage and Armor Threshold Calcs - Mobs.csv";

    public static final ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();

    public static final String NUMBER_REGEX = "-?\\d+";

    /**
     * Exceptions that have a hardcoded max rank. These are mobs such as summons.
     */
    private static final boolean DO_MAX_RANK_EXCEPTIONS = true;
    private static final Map<String, Integer> MOB_MAX_RANK_EXCEPTIONS = Map.ofEntries(
            Map.entry("minecraft:vex", 2),
            Map.entry("minecraft:silverfish", 2),
            Map.entry("occultism:wild_horde_silverfish", 2),
            Map.entry("rottencreatures:scarab", 2),
            Map.entry("born_in_chaos_v1:senor_pumpkin", 2),
            Map.entry("born_in_chaos_v1:baby_spider", 2),
            Map.entry("born_in_chaos_v1:maggot", 2),
            Map.entry("born_in_chaos_v1:corpse_fly", 2),
            Map.entry("born_in_chaos_v1:baby_skeleton", 2),
            Map.entry("born_in_chaos_v1:bone_imp", 2),
            Map.entry("born_in_chaos_v1:siamese_skeletonsright", 2),
            Map.entry("born_in_chaos_v1:siamese_skeletonsleft", 2),
            Map.entry("born_in_chaos_v1:spirit_guide_assistant", 2)
    );

    /**
     * Hardcoded universally added affixList. Use only if you're using exclusively BLACKLIST or WHITELIST.
     * IMPORTANT: THESE MUST BE DOUBLE QUOTED.
     */
    private static final boolean DO_UNIVERSAL_AFFIX_LIST = true;
    private static final Set<String> UNIVERSAL_AFFIX_LIST = Set.of(
            "\"champions:plagued\"",
            "\"champions:reflective\"",
            "\"champions:shielding\"",
            "\"champions:infested\""
    );

    /**
     * Hardcoded universally added presetAffix. Use only if you want EVERY Champion to have this affix.
     * IMPORTANT: THESE MUST BE DOUBLE QUOTED.
     */
    private static final boolean DO_UNIVERSAL_PRESET_AFFIXES = false;
    private static final Set<String> UNIVERSAL_PRESET_AFFIX_LIST = Set.of();

    /**
     * Generates all mob JSON into a config format, from the logs.
     */
    public static void readCsvToEntitiesConfig_All() {
        System.out.println("#List of entity configurations");
        readCsvToEntitiesConfig(CSV_PATH_MOB);
    }

    /**
     * Generates the mob JSON file into a config format, from the logs.
     */
    private static void readCsvToEntitiesConfig(String fileName) {
        LinkedList<MobEntryRow> mobEntryRowSet = new LinkedList<>();
        try (CSVReader reader = new CSVReader(new FileReader("src/main/resources/" + fileName))) {
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                if (isValidRow(nextLine)) {
                    mobEntryRowSet.add(convertRowToMobEntryRow(nextLine));
                }
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        mobEntryRowSet.forEach(System.out::println);
        Path pathToChampionsEntities = Path.of("../config/champions-entities.toml");
        try {
            Files.createDirectories(pathToChampionsEntities.getParent());
            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append("#List of entity configurations\n");
            for (MobEntryRow mobEntry : mobEntryRowSet) {
                contentBuilder.append(mobEntry);
            }
            Files.write(pathToChampionsEntities, contentBuilder.toString().getBytes());
            System.out.println("Wrote champions-entities in: " + pathToChampionsEntities.toAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Path pathToAllowChampions = Path.of("../data/champions/tags/entity_type/allow_champions.json");
        try {
            Files.createDirectories(pathToAllowChampions.getParent());
            AllowChampions allowChampions = new AllowChampions(
                    mobEntryRowSet.stream()
                            .map(MobEntryRow::getMobId)
                            .toList()
            );
            Files.write(pathToAllowChampions, writer.writeValueAsString(allowChampions)
                    .replace(",", ",\n   ").getBytes());
            System.out.println(writer.writeValueAsString(allowChampions));
            System.out.println("Wrote allow_champions in: " + pathToAllowChampions.toAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Asserts that Row will convert into a mob entry correctly.
     * @param rowValues The CSV row
     * @return true if necessary data present, false if even one missing.
     */
    private static boolean isValidRow(String[] rowValues) {
        return StringUtils.isNotBlank(rowValues[0])
                && StringUtils.isNotBlank(rowValues[1]) && rowValues[1].matches(NUMBER_REGEX)
                && StringUtils.isNotBlank(rowValues[2]) && rowValues[2].matches(NUMBER_REGEX)
                && StringUtils.isNotBlank(rowValues[5]);
    }

    /**
     * Ensures comma separated affix list returns correctly.
     * @return A separated by quotes list, e.g.
     * String "champions:knocking, champions:wounding"
     * -> String "\"champions:knocking\", \"champions:wounding\"".
     *
     * Also handles universal affixes.
     */
    private static Set<String> convertAffixList(
            String rawAffixList,
            boolean doUniversalAffixes,
            Set<String> universalAffixes
    ) {
        if (StringUtils.isBlank(rawAffixList)) {
            return doUniversalAffixes ? universalAffixes : Collections.emptySet();
        }
        Set<String> configuredAffixList = Arrays.stream(rawAffixList.split(","))
                .map(rawAffix -> StringUtils.replaceChars(rawAffix, " ", ""))
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.toSet());
        if (doUniversalAffixes) {
            configuredAffixList.addAll(universalAffixes);
        }

        return configuredAffixList;
    }

    /**
     * Mapping from String array of CSV line -> MobDataEntry model.
     * @param nextLine The raw CSV line String[]
     * @return MobEntryRow with config entry data.
     */
    private static MobEntryRow convertRowToMobEntryRow(String[] nextLine) {
        return MobEntryRow.builder()
                .mobId(nextLine[0])
                .minRank(Integer.parseInt(nextLine[1]))
                .maxRank(DO_MAX_RANK_EXCEPTIONS && MOB_MAX_RANK_EXCEPTIONS.containsKey(nextLine[0])
                        ? MOB_MAX_RANK_EXCEPTIONS.get(nextLine[0])
                        : Integer.parseInt(nextLine[2]))
                .affixPresets(convertAffixList(nextLine[3], DO_UNIVERSAL_PRESET_AFFIXES, UNIVERSAL_PRESET_AFFIX_LIST))
                .affixList(convertAffixList(nextLine[4], DO_UNIVERSAL_AFFIX_LIST, UNIVERSAL_AFFIX_LIST))
                .affixListType(nextLine[5])
                .build();
    }

}
