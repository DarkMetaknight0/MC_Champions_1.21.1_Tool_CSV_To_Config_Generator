package com.champions_generator;

import com.champions_generator.service.CsvReaderService;

public class ChampionsGeneratorApplication {

    public static void main(String[] args) {
        CsvReaderService.readCsvToEntitiesConfig_All();
    }

}
