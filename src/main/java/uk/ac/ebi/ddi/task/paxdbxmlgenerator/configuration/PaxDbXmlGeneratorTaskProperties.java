package uk.ac.ebi.ddi.task.paxdbxmlgenerator.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("paxdb")
public class PaxDbXmlGeneratorTaskProperties {
    private int entriesPerFile = 30;
    private String prefix = "PaxDB-";
    private String version = "4.1";
    private String outputDir;

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getEntriesPerFile() {
        return entriesPerFile;
    }

    public void setEntriesPerFile(int entriesPerFile) {
        this.entriesPerFile = entriesPerFile;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
