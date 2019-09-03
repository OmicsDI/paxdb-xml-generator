package uk.ac.ebi.ddi.task.paxdbxmlgenerator.client;

import uk.ac.ebi.ddi.task.paxdbxmlgenerator.model.PaxDBDataset;

import java.io.*;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 27/01/2017.
 */
public class PaxDBDatasetReader {

    private PaxDBDatasetReader() {
    }

    enum PaxDBProperty {

        NAME("name"),
        SCORE("score"),
        WEIGHT("weight"),
        DESCRIPTION("description"),
        ORGAN("organ"),
        INTEGRATED("integrated"),
        COVERAGE("coverage"),
        PUBLICATION_YEAR("publication_year"),
        FILENAME("filename"),
        INTERNAL_ID("internal_id");

        private String name;

        PaxDBProperty(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static PaxDBDataset readDataset(String inputFile) throws IOException {
        PaxDBDataset dataset = new PaxDBDataset();
        try (InputStream is = new FileInputStream(inputFile)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                    break;
                }
                Map.Entry<String, String> property = readProperty(line);
                if (property != null) {
                    if (property.getKey().equalsIgnoreCase(PaxDBProperty.NAME.getName())) {
                        dataset.setName(property.getValue());
                    }
                    if (property.getKey().equalsIgnoreCase(PaxDBProperty.SCORE.getName())) {
                        dataset.setScore(property.getValue());
                    }
                    if (property.getKey().equalsIgnoreCase(PaxDBProperty.WEIGHT.getName())) {
                        dataset.setWeigth(property.getValue());
                    }
                    if (property.getKey().equalsIgnoreCase(PaxDBProperty.DESCRIPTION.getName())) {
                        dataset.setDescription(property.getValue());
                    }
                    if (property.getKey().equalsIgnoreCase(PaxDBProperty.ORGAN.getName())) {
                        dataset.setTissue(property.getValue());
                    }
                    if (property.getKey().equalsIgnoreCase(PaxDBProperty.PUBLICATION_YEAR.getName())) {
                        dataset.setPublicationDate(property.getValue());
                    }
                    if (property.getKey().equalsIgnoreCase(PaxDBProperty.INTEGRATED.getName())) {
                        dataset.setIntegrated(property.getValue());
                    }
                    if (property.getKey().equalsIgnoreCase(PaxDBProperty.COVERAGE.getName())) {
                        dataset.setCoverage(property.getValue());
                    }
                    if (property.getKey().equalsIgnoreCase(PaxDBProperty.FILENAME.getName())) {
                        dataset.setFileName(property.getValue());
                    }
                } else if (line.contains(PaxDBProperty.INTERNAL_ID.getName())) {
                    Map<String, Map.Entry<String, String>> absoluteAbundances = readAbsoluteAbundances(br);
                    if (absoluteAbundances.size() > 0) {
                        dataset.setAbundanceProteins(absoluteAbundances);
                    }
                }
            }
        }
        return dataset;
    }

    private static Map<String, Map.Entry<String, String>> readAbsoluteAbundances(BufferedReader br) throws IOException {
        String line;
        Map<String, Map.Entry<String, String>> mapAbundances = new HashMap<>();
        while ((line = br.readLine()) != null) {
            String[] lineArr = line.split("\\s+");
            if (lineArr.length == 3) {
                String idProtein = lineArr[1];
                String abundance = lineArr[2];
                mapAbundances.put(idProtein, new AbstractMap.SimpleEntry<>(abundance, null));
            } else if (lineArr.length == 4) {
                String idProtein = lineArr[1];
                String abundance = lineArr[2];
                String rawSpectralCount = lineArr[3];
                mapAbundances.put(idProtein, new AbstractMap.SimpleEntry<>(abundance, rawSpectralCount));
            }
        }
        return mapAbundances;
    }

    private static Map.Entry<String, String> readProperty(String line) {

        String[] lineArr = line.replaceFirst("#", "").split(":", 2);
        if (lineArr.length == 2) {
            return new AbstractMap.SimpleEntry<>(lineArr[0].trim(), lineArr[1].trim());
        }
        return null;
    }

    private static Map.Entry<String, String> readFileIdentifierMap(String line) {
        String[] lineArr = line.split("\\s+");
        if (lineArr.length == 2) {
            return new AbstractMap.SimpleEntry<>(lineArr[0].trim(), lineArr[1].trim());
        }
        return null;
    }

    public static Map<String, String> readMapFileIdentifiers(ByteArrayOutputStream file) throws IOException {
        Map<String, String> identifierMap = new HashMap<>();
        InputStream is = new ByteArrayInputStream(file.toByteArray());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            if (!line.startsWith("#")) {
                Map.Entry<String, String> property = readFileIdentifierMap(line);
                if (property != null) {
                    identifierMap.put(property.getKey(), property.getValue());
                }
            }
        }
        return identifierMap;
    }

    public static Map<String, String> readProteinIdentifiers(ByteArrayOutputStream zipInputStreamProteinFiles)
            throws IOException {
        Map<String, String> proteinIds = new HashMap<>();
        InputStream is = new ByteArrayInputStream(zipInputStreamProteinFiles.toByteArray());
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = br.readLine()) != null) {
            if (line.split("\\s+").length == 2) {
                String[] idArr = line.split("\\s+");
                proteinIds.put(idArr[0], idArr[1]);
            }
        }
        return proteinIds;
    }
}
