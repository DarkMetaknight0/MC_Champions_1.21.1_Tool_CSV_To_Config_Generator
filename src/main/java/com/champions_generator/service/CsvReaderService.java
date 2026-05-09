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

public class CsvReaderService {

    public static final String CSV_PATH_MOB = "Damage_Armor_Threshold_Calcs_Mobs.csv";

    public static final ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();

    /**
     * Exceptions that have a hardcoded max rank. These are mobs such as summons.
     */
    public static final Map<String, Integer> mobMaxRankExceptions = Map.of(
            "minecraft:vex", 2,
            "minecraft:silverfish", 2,
            "occultism:wild_horde_silverfish", 2,
            "rottencreatures:scarab", 2
    );

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
            reader.skip(6); // skip headers
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                mobEntryRowSet.add(convertRowToMobEntryRow(nextLine));
            }
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
        mobEntryRowSet.forEach(System.out::println);
        Path pathToChampionsEntities = Path.of("../config/champions_entities.toml");
        try {
            Files.createDirectories(pathToChampionsEntities.getParent());
            StringBuilder contentBuilder = new StringBuilder();
            contentBuilder.append("#List of entity configurations\n");
            for (MobEntryRow mobEntry : mobEntryRowSet) {
                contentBuilder.append(mobEntry);
            }
            Files.write(pathToChampionsEntities, contentBuilder.toString().getBytes());
            System.out.println("Wrote champions_entities in: " + pathToChampionsEntities.toAbsolutePath());
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
     * Ensures comma separated affix list returns correctly.
     * @return A separated by quotes list, e.g.
     * String "champions:knocking, champions:wounding"
     * -> String "\"champions:knocking\", \"champions:wounding\""
     */
    private static List<String> convertAffixList(String rawAffixList) {
        if (StringUtils.isBlank(rawAffixList)) {
            return new ArrayList<>();
        }
        return Arrays.stream(rawAffixList.split(","))
                .map(rawAffix -> StringUtils.replaceChars(rawAffix, " ", ""))
                .map(s -> "\"" + s + "\"")
                .toList();
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
                .maxRank(mobMaxRankExceptions.containsKey(nextLine[0])
                        ? mobMaxRankExceptions.get(nextLine[0])
                        : Integer.parseInt(nextLine[2]))
                .affixPresets(convertAffixList(nextLine[3]))
                .affixList(convertAffixList(nextLine[4]))
                .affixListType(nextLine[5])
                .build();
    }

}
