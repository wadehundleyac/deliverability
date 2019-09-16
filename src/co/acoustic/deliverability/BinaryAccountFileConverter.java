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

import com.port25.pmta.api.accounter.AccountingReader;
import java.io.IOException;

<<<<<<< HEAD
public class BinaryAccountFileConverter extends AccountFileConverter  {
=======

public class BinaryAccountFileConverter implements AccountFileConverter  {
>>>>>>> origin/develop

    public int processFile(String inputFile, String outputDir, int maxRecordsPerFile )
    {
        int recordsProcessed=0;

        AccountingReader acc = new AccountingReader(this);
        try {
            acc.open(inputFile, 0);
            setFileName(inputFile);    // file to process
            setOutputDir(outputDir); // where to write csv
            initRecord();            // init the record before the first pass

            if (maxRecordsPerFile > 0)
                setFileSize(maxRecordsPerFile);

            while (acc.read()) {
                    /* do per-record processing here */
                endRecord();
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
