package uk.ac.ebi.ddi.task.paxdbxmlgenerator.model;

import uk.ac.ebi.ddi.api.readers.model.IAPIDataset;
import uk.ac.ebi.ddi.api.readers.utils.Constants;
import uk.ac.ebi.ddi.ddidomaindb.dataset.DSField;
import uk.ac.ebi.ddi.xml.validator.utils.BiologicalDatabases;
import uk.ac.ebi.ddi.xml.validator.utils.OmicsType;

import java.util.*;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 03/12/2015
 */

public class PaxDBDataset implements IAPIDataset {

    private String identifier = null;

    private String name;

    private String score;

    private String weigth;

    private String description;

    private String tissue;

    private String integrated;

    private String coverage;

    private String publicationDate;

    private String fileName;

    private String fullLink;

    private String listProteins;

    private Map<String, Map.Entry<String, String>> abundanceProteins;

    public String getIdentifier() {
        if (fullLink != null) {
            String id = fullLink.replace(Constants.PAXDB_URL, "");
            StringBuilder finalId = new StringBuilder();
            Arrays.asList(id.split("/")).forEach(key -> {
                if (key != null && key.length() > 0) {
                    finalId.append(key);
                }
            });
            identifier = finalId.toString();
        }
        return identifier;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public void setWeigth(String weigth) {
        this.weigth = weigth;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getDataProtocol() {
        return Constants.PAXDB_DATA_PROTOCOL;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTissue() {
        return tissue;
    }

    public void setTissue(String tissue) {
        this.tissue = tissue;
    }

    public String getIntegrated() {
        return integrated;
    }

    public void setIntegrated(String integrated) {
        this.integrated = integrated;
    }

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public String getPublicationDate() {
        return Constants.PAXDB_RELEASE_DATE;
    }

    @Override
    public Map<String, String> getOtherDates() {
        return Collections.EMPTY_MAP;
    }

    @Override
    public String getSampleProcotol() {
        return Constants.EMPTY_STRING;
    }

    @Override
    public Set<String> getOmicsType() {
        Set<String> omicsType = new HashSet<>();
        omicsType.add(OmicsType.PROTEOMICS.getName());
        return omicsType;
    }

    @Override
    public String getRepository() {
        return Constants.PAXDB;
    }

    public void setPublicationDate(String publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFullLink() {
        return fullLink;
    }

    @Override
    public Set<String> getInstruments() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getSpecies() {
        Set<String> species = new HashSet<>();
        if (name != null && name.length() > 0) {
            if (name.split("-").length > 1) {
                species.add(name.split("-")[0].trim());
            }
        }
        return species;
    }

    @Override
    public Set<String> getCellTypes() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getDiseases() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getTissues() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getSoftwares() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getSubmitter() {
        Set<String> submistters = new HashSet<>();
        submistters.add(Constants.PAXDB_CONTACT_TEAM);
        return submistters;
    }

    @Override
    public Set<String> getSubmitterEmails() {
        Set<String> emails = new HashSet<>();
        emails.add(Constants.PAXDB_CONTACT);
        return emails;
    }

    @Override
    public Set<String> getSubmitterAffiliations() {
        Set<String> affiliations = new HashSet<>();
        affiliations.add(Constants.PAXDB_AFILLIATION);
        return affiliations;
    }

    @Override
    public Set<String> getSubmitterKeywords() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getLabHead() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getLabHeadMail() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getLabHeadAffiliation() {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getDatasetFiles() {
        Set<String> files = new HashSet<>();
        files.add(Constants.PAXDB_DATASET_FILE);
        return files;
    }

    @Override
    public Map<String, Set<String>> getCrossReferences() {
        final String[] pubmedID = new String[1];
        pubmedID[0] = null;
        Map<String, Set<String>> crossReferences = new HashMap<>();
        Set<String> ids = new HashSet<>();
        ids.add(Constants.PAXDB_PUBMED);
        //Add publication of the dataset
        if (description != null && description.length() > 0 && description.contains("pubmed")) {
            Arrays.asList(description.split("&")).forEach(key -> {
                if (key.contains("id=")) {
                    pubmedID[0] = key.split("=")[1];
                }
            });
        }
        if (pubmedID[0] != null) {
            ids.add(pubmedID[0]);
        }
        crossReferences.put(DSField.CrossRef.PUBMED.getName(), ids);

        Set<String> proteins = new HashSet<>();
        if (abundanceProteins != null && abundanceProteins.size() > 0) {
            abundanceProteins.forEach((key, value) -> proteins.add(key));
        }
        if (proteins.size() > 0) {
            crossReferences.put(BiologicalDatabases.UNIPROT.getName(), proteins);
        }

        return crossReferences;
    }

    @Override
    public Map<String, Set<String>> getOtherAdditionals() {
        return Collections.emptyMap();
    }

    public void setFullLink(String fullLink) {
        this.fullLink = fullLink;
    }

    public String getListProteins() {
        return listProteins;
    }

    public void setListProteins(String listProteins) {
        this.listProteins = listProteins;
    }

    public void setAbundanceProteins(Map<String, Map.Entry<String, String>> abundanceProteins) {
        this.abundanceProteins = abundanceProteins;
    }

    public void updateIds(Map<String, String> correctIds) {
        Map<String, Map.Entry<String, String>> abundances = new HashMap<>();
        if (abundanceProteins != null && abundanceProteins.size() > 0) {
            abundanceProteins.forEach((key, value) -> {
                abundances.put(correctIds.getOrDefault(key, key), value);
            });
        }
        abundanceProteins = abundances;
    }
}
