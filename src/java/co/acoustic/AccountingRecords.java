package co.acoustic;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gwhundle on 8/28/19.
 */
public class AccountingRecords {

    Map<String, String> fieldMapping = new HashMap<String, String>() {
        {
            put("type", "type");
            put("timeLogged", "t");
            put("timeQueued", "tq");
            put("orig", "orig");
            put("rcpt", "r");
            put("orcpt", "or");
            put("dsnAction", "dsnAct");
            put("dsnStatus", "dsnSts");
            put("dsnDiag", "dsnDiag");
            put("dsnMta", "dsnMTA");
            put("srcType", "src.type");
            put("srcMta", "src.mta");
            put("dlvType", "dlvThrough");
            put("dlvSourceIp", "dlvFrom");
            put("dlvDestinationIp", "dlvTo");
            put("dlvEsmtpAvailable", "esmtp");
            put("dlvSize", "size");
            put("vmta", "vmta");
            put("jobId", "jobId");
            put("envId", "envId");
            put("queue", "");
            put("vmtaPool", "");
            put("bounceCat", "bncCat");
            put("vmtaPool", "");
        }
    };

    // convert CSV Field Name to Binary Field Name
    public String convertCsvToBinary( String csvField )
    {
        return fieldMapping.get(csvField);
    }



}
