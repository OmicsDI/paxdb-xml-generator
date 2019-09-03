package uk.ac.ebi.ddi.task.paxdbxmlgenerator.services;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import uk.ac.ebi.ddi.api.readers.utils.FileUtils;
import uk.ac.ebi.ddi.task.paxdbxmlgenerator.client.PaxDBDatasetReader;
import uk.ac.ebi.ddi.task.paxdbxmlgenerator.utils.FileDownloadUtils;
import uk.ac.ebi.ddi.task.paxdbxmlgenerator.utils.ZipUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaxDBService {

    private static final String PAX_ENDPOINT = "https://pax-db.org/downloads/latest";

    private File datasetDir = new File("datasets");

    public PaxDBService() throws IOException {
        if (!datasetDir.exists()) {
            Files.createDirectories(datasetDir.toPath());
        }
    }

    public List<String> getDatasetFiles(String version) throws IOException {
        File tmpZip = File.createTempFile("ddi", "downloader.zip");
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(PAX_ENDPOINT)
                .path("/datasets")
                .path(String.format("/paxdb-abundance-files-v%s.zip", version));
        FileDownloadUtils.downloadFile(builder.toUriString(), tmpZip);
        ZipUtils.unzip(tmpZip, datasetDir);
        String fileName = FilenameUtils.getBaseName(builder.toUriString());
        return Files.walk(datasetDir.toPath())
                .filter(Files::isRegularFile)
                .filter(x -> !isAlreadyExists(x, fileName))
                .map(x -> x.toAbsolutePath().toString())
                .collect(Collectors.toList());
    }

    public Map<String, String> getIdentifiers() throws IOException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(PAX_ENDPOINT)
                .path("/omicsdb_export.tsv");
        File tmpTsv = File.createTempFile("ddi", "omicsdb.tsv");
        FileDownloadUtils.downloadFile(builder.toUriString(), tmpTsv);
        ByteArrayOutputStream zipInputStreamFile = FileUtils.doInputStream("file://" + tmpTsv.getAbsolutePath());
        return PaxDBDatasetReader.readMapFileIdentifiers(zipInputStreamFile);
    }

    public Map<String, String> getProteins(String version) throws IOException {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(PAX_ENDPOINT)
                .path(String.format("/paxdb-uniprot-links-v%s.zip", version));
        File tmpZip = File.createTempFile("ddi", "links.zip");
        FileDownloadUtils.downloadFile(builder.toUriString(), tmpZip);
        Map<String, ByteArrayOutputStream> outputStream =
                FileUtils.doZipInputStream("file://" + tmpZip.getAbsolutePath());
        Map<String, String> proteins = new HashMap<>();
        for (Map.Entry<String, ByteArrayOutputStream> entry : outputStream.entrySet()) {
            if (entry.getValue() != null) {
                proteins.putAll(PaxDBDatasetReader.readProteinIdentifiers(entry.getValue()));
            }
        }
        return proteins;
    }

    private boolean isAlreadyExists(Path path, String baseName) {
        // This is the issue of Pax dataset zip file version 4.1
        return path.toAbsolutePath().toString().contains(baseName + File.separator + baseName);
    }
}
