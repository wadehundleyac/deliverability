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

import java.util.HashMap;
import java.util.Map;


public class AccountingFields {

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
            put("srcMta", "mta");
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
