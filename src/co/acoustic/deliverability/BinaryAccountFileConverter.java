package co.acoustic.deliverability;

import com.port25.pmta.api.accounter.AccountingReader;
import java.io.IOException;


public class BinaryAccountFileConverter implements AccountFileConverter  {

    public int processFile(String inputFile, String outputDir, int maxRecordsPerFile )
    {
        int recordsProcessed=0;

        PmtaAccountingProcessor accProc = new PmtaAccountingProcessor(outputDir);
        AccountingReader acc = new AccountingReader(accProc);
        try {
            acc.open(inputFile, 0);
            accProc.setFileName(inputFile);    // file to process
            accProc.setOutputDir(outputDir); // where to write csv
            accProc.initRecord();            // init the record before the first pass

            if (maxRecordsPerFile > 0)
                accProc.setFileSize(maxRecordsPerFile);

            while (acc.read()) {
                    /* do per-record processing here */
                accProc.endRecord();
                recordsProcessed++;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            acc.close();
        }

        return recordsProcessed;
    }
}
