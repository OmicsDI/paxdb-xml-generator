package uk.ac.ebi.ddi.task.paxdbxmlgenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.ac.ebi.ddi.api.readers.utils.Constants;
import uk.ac.ebi.ddi.api.readers.utils.FileUtils;
import uk.ac.ebi.ddi.api.readers.utils.Transformers;
import uk.ac.ebi.ddi.ddifileservice.services.IFileSystem;
import uk.ac.ebi.ddi.ddifileservice.type.ConvertibleOutputStream;
import uk.ac.ebi.ddi.task.paxdbxmlgenerator.client.PaxDBDatasetReader;
import uk.ac.ebi.ddi.task.paxdbxmlgenerator.configuration.PaxDbXmlGeneratorTaskProperties;
import uk.ac.ebi.ddi.task.paxdbxmlgenerator.model.PaxDBDataset;
import uk.ac.ebi.ddi.task.paxdbxmlgenerator.services.PaxDBService;
import uk.ac.ebi.ddi.xml.validator.parser.marshaller.OmicsDataMarshaller;
import uk.ac.ebi.ddi.xml.validator.parser.model.Database;
import uk.ac.ebi.ddi.xml.validator.parser.model.Entry;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class PaxdbXmlGeneratorApplication implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(PaxdbXmlGeneratorApplication.class);

	@Autowired
	private PaxDBService paxDBService;

	@Autowired
	private PaxDbXmlGeneratorTaskProperties taskProperties;

	@Autowired
	private IFileSystem fileSystem;

	public static void main(String[] args) {
		SpringApplication.run(PaxdbXmlGeneratorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		fileSystem.cleanDirectory(taskProperties.getOutputDir());
		List<String> datasetFiles = paxDBService.getDatasetFiles(taskProperties.getVersion());
		Map<String, String> mapIdentifiers = paxDBService.getIdentifiers();
		Map<String, String> proteins = paxDBService.getProteins(taskProperties.getVersion());
		List<Entry> entries = new ArrayList<>();
		AtomicInteger fileCount = new AtomicInteger(0);
		for (String file : datasetFiles) {
			try {
				PaxDBDataset dataset = PaxDBDatasetReader.readDataset(file);
				String key = FileUtils.getNameFromInternalZipPath(file);
				if (mapIdentifiers.containsKey(key)) {
					dataset.setFullLink(mapIdentifiers.get(key));
				}
				dataset.updateIds(proteins);
				if (dataset.getIdentifier() != null) {
					entries.add(Transformers.transformAPIDatasetToEntry(dataset));
				}
				if (entries.size() % taskProperties.getEntriesPerFile() == 0) {
					writeDatasetsToFile(entries, datasetFiles.size(), fileCount.getAndIncrement());
				}
			} catch (Exception e) {
				LOGGER.error("Exception occurred when reading file {}, ", file, e);
			}
		}
		writeDatasetsToFile(entries, datasetFiles.size(), fileCount.getAndIncrement());
	}

	private void writeDatasetsToFile(List<Entry> entries, int total, int fileCount) throws IOException {
		if (entries.size() < 1) {
			return;
		}

		String releaseDate = new SimpleDateFormat("yyyyMMdd").format(new Date());

		ConvertibleOutputStream outputStream = new ConvertibleOutputStream();
		try (Writer w = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)) {
			OmicsDataMarshaller mm = new OmicsDataMarshaller();

			Database database = new Database();
			database.setDescription(Constants.PAXDB_DESCRIPTION);
			database.setName(Constants.PAXDB);
			database.setRelease(releaseDate);
			database.setEntries(entries);
			database.setEntryCount(total);
			mm.marshall(database, w);
		}

		String filePath = taskProperties.getOutputDir() + "/" + taskProperties.getPrefix() + fileCount + ".xml";
		LOGGER.info("Attempting to write data to {}", filePath);
		fileSystem.saveFile(outputStream, filePath);
		entries.clear();
	}
}
