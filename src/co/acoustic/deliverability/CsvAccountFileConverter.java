package co.acoustic.deliverability;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;


public class CsvAccountFileConverter implements AccountFileConverter {

    public int processFile(String inputFile, String outputDir, int maxRecordsPerFile)
    {
        int recordsProcessed=0;

        AccountingFields accRecords = new AccountingFields();
        PmtaAccountingProcessor accProc = new PmtaAccountingProcessor(outputDir);

        try {
            accProc.setFileName(inputFile);    // file to process
            accProc.setOutputDir(outputDir); // where to write csv
            accProc.initRecord();            // init the record before the first pass

            if (maxRecordsPerFile > 0)
                accProc.setFileSize(maxRecordsPerFile);

            File csvFile = new File(inputFile);
            CsvMapper mapper = new CsvMapper();
            CsvSchema schema = CsvSchema.emptySchema().withHeader(); // use first row as header; otherwise defaults are fine
            MappingIterator<Map<String, String>> it = mapper.readerFor(Map.class)
                    .with(schema)
                    .readValues(csvFile);

            // loop per row
            while (it.hasNext()) {
                Map<String, String> rowAsMap = it.next();
                // access by column name, as defined in the header row...

                // loop per field in a row
                Iterator<Map.Entry<String, String>> fieldItr = rowAsMap.entrySet().iterator();
                while (fieldItr.hasNext()) {
                    Map.Entry<String, String> field = fieldItr.next();
                    accProc.processField(accRecords.convertCsvToBinary(field.getKey()), null, field.getValue());
                }

                accProc.endRecord();
                recordsProcessed++;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            accProc.closeFile();
            accProc.closeFileB2B();
        }

        return recordsProcessed;
    }
}
