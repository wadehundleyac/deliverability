/****************************************************************
 *
 * Copyright © 2019 Acoustic, L.P. All rights reserved.
 *
 * NOTICE: This file contains material that is confidential and proprietary to
 * Acoustic, L.P. and/or other developers. No license is granted under any intellectual or
 * industrial property rights of Acoustic, L.P. except as may be provided in an agreement with
 * Acoustic, L.P. Any unauthorized copying or distribution of content from this file is
 * prohibited.
 *
 ****************************************************************/



package co.acoustic.deliverability;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class CsvAccountFileConverter extends AccountFileConverter {

    public int processFile(String inputFile, String outputDir, int maxRecordsPerFile)
    {
        int recordsProcessed=0;

        AccountingFields accRecords = new AccountingFields();

        try {
            setFileName(inputFile);    // file to process
            setOutputDir(outputDir); // where to write csv
            initRecord();            // init the record before the first pass

            if (maxRecordsPerFile > 0)
                setFileSize(maxRecordsPerFile);

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
                    processField(accRecords.convertCsvToBinary(field.getKey()), null, field.getValue());
                }

                endRecord();
                recordsProcessed++;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            closeFile();
            closeFileB2B();
        }

        return recordsProcessed;
    }
}
