/****************************************************************
 *
 * Copyright © 20xx Acoustic, L.P. All rights reserved.
 *
 * NOTICE: This file contains material that is confidential and proprietary to
 * Acoustic, L.P. and/or other developers. No license is granted under any intellectual or
 * industrial property rights of Acoustic, L.P. except as may be provided in an agreement with
 * Acoustic, L.P. Any unauthorized copying or distribution of content from this file is
 * prohibited.
 *
 ****************************************************************/


/*
* Program Name log_processing.java
* Build to: logprocess
* Author: T Roberts
* Date :  9 Jan 2008 - Initial version
*
* Purpose: Read a port25 MTA accounting file and convert it to a csv for loading into a data base
*
* Compile: /usr/local/jdk1.5.0_10-patched/bin/javac -classpath /usr/local/apps/lib/pmta.jar:/usr/local/apps/lib/spcore.jar log_processing.java
*
* Execute:/usr/local/jdk1.5.0_10-patched/bin/java -classpath .:/usr/local/apps/lib/pmta.jar:/usr/local/apps/lib/spcore.jar -Djava.library.path=/usr/lib logprocess.log_processing /var/pmtalogs/mx01.atlp1.2008.01.06.00.18 /var/pmtalogs/temp
*
* Note:  Performance is effected by the large number of string comparisons.
*
* Initial Time Trials:
*  Binary File Size: 1,017,166,964
*  Resulting Number of Rows: 3,620,331
*  Time to create: 15 minutes 5 seconds
*
*  Change - 10 Jan 2008 - T. Roberts
*	Removed extra space after comma when writing the file
*
*  Change - 11 Jan 2008 - T. Roberts
*	Added code to remove comma from dsnCode field.
*
*  Change - 14 Jan 2008 - T. Roberts
*	Modified the test for valid record in end structure function.  When an invalid time was discovered
* 	a function return was executed without re-initializing the record.  Code changed to initialize the
*	record prior to the function return.
*
*  Change - 14 Jan 2008 - T. Roberts
*	Moved code from endStructure to endRecord processing
*
*  Change - 16 Jan 2008 - T. Roberts
*   Modified getTimeStamp to explicitly use a calandar object to
*	set the timezone to GMT.  SimpleDateFormat was using the local
*   time zone.
*   Also, modified to set getTimeStamp result to "" if the value
*   to be converted is 0
*   Add default value to bounceFamily of "Delivered"
*
*  Change - 23 Jan 2008 - T. Roberts
*	Added a flush statement after a fp.write, file was closing prior to full buffer flush.
*   Added an optional 3rd parameter to the commandline.  It is the number of rows to include
*   in each file.  Defaults to 1MM
*
*  Change - 10 Nov 2008 - T. Roberts
* 	Added code to fix Yahoo! bounce cats.  All bounces showing up as 'other', parsing
*	dsnDiag to discover actual reason and changing bncCat to reflect actual reason
*	Request made by Chris A of the deliverability team.
*/

package co.acoustic.deliverability;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.io.FilenameUtils;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.databind.MappingIterator;

import com.port25.pmta.api.accounter.AccountingProcessor;
import com.port25.pmta.api.accounter.AccountingReader;

import co.acoustic.deliverability.AccountingRecords;

import com.silverpop.core.util.Verp;


/*
 * Example for accessing the PowerMTA Accounter API / its accounting data
 * from Java.
 *
 * Copyright 2002 - 2004 by Port25 Solutions, Inc.
 * All rights reserved.
 *
 * $Id: ExampleAccounter.java,v 1.11 2004/04/19 12:57:51 robert Exp $
 */
public class log_processing implements AccountingProcessor {

    private String desiredIp;
    private String lastIp;
    private int thisMessageSize;
    private int totalMessageSize;

    private int isFrom = 0;
    private int isSSID = 0;

    // Max lines per file
    private int maxLinesPerFile = 1000000;
    //private int maxLinesPerFile = 250000;

    private int currentLine = 0;
    private int currentLineB2B = 0;

    private int level = 0;
    private int levelB2B = 0;

    //  the record variables that we want
    private String t;
    private long t1;
    private String tq;
    private long tq1;
    private String envId;
    private long jobId;
    private long reportId;
    private String dlvFrom;
    private String dlvTo;
    private String orig;
    private int nRcpt;
    private int size;
    private String r;
    private String tr;
    private long tr1;
    private String ti;
    private String dsnAct;
    private String dsnSts;
    private String dsnMTA;
    private String dsnDiag;
    private String bncCat;
    private String type;
    private String mta;
    private String vmta;
    private long msgid;
    private long recpid;
    private long retry;
    private String domain;
    //new stuff below
    private String fromAddress;
    private String ssID;

    private BufferedWriter fp;
    private BufferedWriter fp2;
    private String baseFileName;
    private String outputDirectory;
    private String baseFilePath;

    private String tempFromDomain;


    log_processing(String ip) {
        desiredIp = ip;
        lastIp = null;
        thisMessageSize = 0;
        totalMessageSize = 0;
    }

    public void setFileSize( int maxRecordsPerFile ){
        maxLinesPerFile = maxRecordsPerFile;
    }

    public void initRecord()
    {
        t = "";
        t1 = 0L;
        tq = "";
        tq1 = 0L;
        envId = "";
        jobId = 0;
        reportId=0;
        dlvFrom = "";
        dlvTo = "";
        orig = "";
        nRcpt = 0;
        r = "";
        tr = "";
        tr1 = 0L;
        ti = "";
        dsnAct = "";
        dsnSts = "";
        dsnMTA = "";
        dsnDiag = "";
        bncCat = "";
        type = "";
        mta = "";
        vmta = "";
        msgid = 0L;
        recpid = 0L;
        retry = 0L;
        domain = "";
        fromAddress ="";
        ssID="";
    }


    public void printRecord( int line )
    {
        System.out.println( line + ": " + t + "," +
                tq + "," +
                envId + "," +
                jobId + "," +
                reportId + "," +
                dlvFrom + "," +
                dlvTo + "," +
                orig + "," +
                nRcpt + "," +
                r + "," +
                tr + "," +
                dsnAct + "," +
                dsnSts + "," +
                dsnMTA + "," +
                dsnDiag + "," +
                bncCat + "," +
                type + "," +
                mta + "," +
                vmta + "," +
                msgid + "," +
                recpid + "," +
                retry + "," +
                domain + "," +
                fromAddress + "," +
                ssID);
    }

    public void printField( int line, String name, String value ){
        System.out.println( "Record: " + line );
        System.out.println( "Name: " + name + " Value[" + value +"]");
    }

    public String getTimeStamp( long t )
    {
        String rslt = "";

        // if t == 0 then the resulting field should be null
        if( t == 0 )
            rslt = "";
        else
        {
            Date t1;
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");

            t1 = new java.util.Date( t * 1000 );

            // If t1 is in GMT use this one
            //rslt = format.format( t1 );

            // if t1 is local use this one
            //String rslt2 = "";
            java.util.Calendar cal0 = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
            format.setCalendar(cal0);
            rslt = format.format(t1);

            //System.out.println( "Original: " + rslt );
            //System.out.println( "New     : " + rslt2);
        }

        return( rslt );
    }

    public int setKeyWords( String lookfor, String cat)
    {
        int rslt = 0;
        String diag;

        diag = lookfor.toLowerCase();

        if( cat.equals( "other") ||
                cat.equals( "policy-related") ||
                cat.equals( "spam-related"))
        {
            if( diag.indexOf("spamcop") > 0)
                rslt = 1;
            else if( diag.indexOf( "spamhaus") > 0 )
                rslt = 2;
            else if( diag.indexOf( "dynablock") > 0 )
                rslt = 3;
            else if( diag.indexOf( "rly:bd") > 0)
                rslt = 4;
            else if( diag.indexOf( "hvu:") > 0 )
                rslt = 5;
            else if( diag.indexOf( "rly:") > 0 )
                rslt = 6;
            else if( diag.indexOf( "dyn:") > 0 )
                rslt = 6;
            else if( diag.indexOf( "blackholes.excite.com") > 0)
                rslt = 7;
            else if( diag.indexOf( "defer-04.html") > 0 )
                rslt = 8;
            else if( diag.indexOf( "many recipient") > 0)
                rslt = 9;
            else if( diag.indexOf( "many connection") > 0)
                rslt = 10;
        }

        return( rslt );
    }

    public int setDomainInfo( String dom, String cat )
    {
        int rslt = 0;
        String ldom;

        ldom = dom.toLowerCase();

        if( ldom.equals( "bellsouth.net") && cat.equals("message-expired"))
            rslt = 11;
        else if( ldom.equals("hotmail.com") && cat.equals( "no-answer-from-host"))
            rslt = 12;
        else if( ldom.indexOf( ".rr.com") > 0 && cat.equals( "message-expired"))
            rslt = 13;
        else if( ldom.indexOf( ".rr.com") > 0 && cat.equals( "policy-related"))
            rslt = 14;

        return( rslt );
    }

    public void setOutputDir( String dir ){
        outputDirectory = dir;
    }

    public void setFileName ( String fn ){
        baseFilePath = FilenameUtils.getPath(fn);
        baseFileName = FilenameUtils.getName(fn);
    }

    public void createFile( ){
        String fName;

        fName = "........................................";
        try{
            fName = outputDirectory + "/" + baseFileName + "." + level + ".csv";
            fp = new BufferedWriter( new FileWriter( fName) );
            level++;
        }
        catch (IOException s){
            System.out.println( "Unable to create file: " + fName );
            s.printStackTrace();
        }

    }

    public void createFileB2B( ){
        String fNameB2B;
        fNameB2B = "........................................";
        try{
            fNameB2B = outputDirectory + "/" + baseFileName + "." + levelB2B + ".B2B.csv";
            fp2 = new BufferedWriter( new FileWriter( fNameB2B) );
            levelB2B++;
        }
        catch (IOException s){
            System.out.println( "Unable to create file: " + fNameB2B );
            s.printStackTrace();
        }
    }

    public void closeFile( ){
        try{
            fp.close();
        }
        catch( IOException s)
        {
            System.out.println( "Unable to close file");
            s.printStackTrace();
        }
    }

    public void closeFileB2B( ){
        try{
            fp2.close();
        }
        catch( IOException s)
        {
            System.out.println( "Unable to close B2B file");
            s.printStackTrace();
        }
    }

    public void beginStructure(String structName, Map attributes) {
        // don't care
        //printField(currentLine, "Begin Record", "x" );
    }


    public void processField(String name, Map attributes, String content) {
        /* While procesing the fields in the accounting record, we do not
           know about the order of the fields, i.e. when we get the IP
           address we might not have the size and vice versa. Thus we
           gather the values for later use. */

        //printField( currentLine, name, content );

        if ( isFrom == 1 )
        {
            if( name.equals("content")) {
                //System.out.println( "From Field Equals " + content );
                fromAddress = content;
                isFrom = 0;
            }
        }
        else if( isSSID == 1 )
        {
            if( name.equals("content")) {
                //System.out.println( "SSID Equals " + content );
                ssID = content;
                isSSID = 0;
            }
        }
        else
        {
            if (name.equals("dlvTo")) {
                dlvTo = content;
            }
            else if ( name.equals("tq")){
                try{
                    tq1 = Long.parseLong(content);
                }
                catch( java.lang.NumberFormatException x )
                {
                    System.out.println( "T1 RETRIEVAL ");
                    //x.printStackTrace() ;
                    tq1=1;
                }
            }
            else if( name.equals("t")){
                try{
                    t1 = Long.parseLong(content);
                }
                catch( java.lang.NumberFormatException x ) {
                    System.out.println( "T1 RETRIEVAL ");
                    //x.printStackTrace();
                    t1=1;
                }
            }
            else if ( name.equals("envId")){
                envId = content;
            }
            else if ( name.equals("dlvFrom")){
                dlvFrom = content;
            }
            else if ( name.equals("dlvTo")){
                dlvTo = content ;
            }
            else if( name.equals("orig")){
                orig = content;
            }
            else if(  name.equals("nRcpt")){
                nRcpt = Integer.parseInt(content);
            }
            else if( name.equals("r")){
                r = content;
            }
            else if( name.equals("tr")){
                try{
                    // If there is a tr record it's a bounce
                    tr1 = Long.parseLong(content);
                }
                catch( java.lang.NumberFormatException x ){
                    System.out.println( "Error processing tr");
                    //x.printStackTrace() ;
                    tr1=1;
                }
            }
            else if( name.equals("dsnAct")){
                dsnAct = content;
            }
            else if( name.equals("dsnSts")){
                dsnSts = content;
            }
            else if( name.equals("dsnMTA")){
                dsnMTA = content;
            }
            else if( name.equals("bncCat")){
                bncCat = content;
            }
            else if( name.equals("mta")){
                mta = content;
                // Debug
                //System.out.println("MTA: [" + mta + "]");
            }
            else if( name.equals( "type")){
                type = content;
            }
            else if( name.equals( "vmta")){
                vmta = content;
            }
            else if( name.equals("ti")){
                ti = content;
            }
            else if( name.equals("dsnDiag")){
                dsnDiag = content;
                int maxLen = 2499;
                if( dsnDiag.length() > maxLen ){
                    dsnDiag = dsnDiag.substring(0,maxLen);
                }
            }
            else if( name.equals("esmtp")){
                //do nothing
            }
            else if( name.equals("dlvThrough")){
                //do nothing
            }
            else if( name.equals("size")){
                try {
                    size = Integer.parseInt(content);
                }
                catch (NumberFormatException nfe) {
                    //nfe.printStackTrace();
                    size=0;
                }
            }
            else if( name.equals("version")){
                //do nothing
            }
            else if( name.equals("fullHostName")){
                //do nothing
            }
            else if( name.equals("usrString")){
                //do nothing
            }
            else if( name.equals("build")){
                //do nothing
            }
            else if( name.equals("count")){
                //do nothing
            }
            else if( name.equals("real")){
                //do nothing
            }
            else if( name.equals("timeNow")){
                //do nothing
            }
            else if( name.equals("startupTime")){
                //do nothing
            }
            else if( name.equals("shuttingDown")){
                //do nothing
            }
            else if( name.equals("rcp")){
                //do nothing
            }
            else if( name.equals("msg")){
                //do nothing
            }
            else if( name.equals("gmimprinted")){
                //do nothing
            }
            else if( name.equals("kb")){
                //do nothing
            }
            else if( name.equals("name")){
                if( content.equals( "From"))
                    isFrom = 1;
                else if(content.equals("imarkssID" ))
                    isSSID = 1;
            }
            else {
                // If you need to discover the fields that are not processed
                //  uncomment the following line and recompile
                //System.out.println( "Unknown: " + name + " [" + content + "]");
            }
        }
    }

    public void endStructure(String structName) {
        // no end of struc processing
    }

    public void endRecord()
    {

        // some fields will have to be manipulated to
        // calculate the needed data.

        // is this even a valid record
        if(  t1 == 0  )
        {
            initRecord();
            //System.out.println( "Bad Record???? " + currentLine );
            return;
        }

        // Convert the long time to a good format
        try{
            t = getTimeStamp( t1 );
        } catch ( Exception s ){
            System.out.println( "Time t: " + s.toString());
            s.printStackTrace();
        }

        try {
            tq = getTimeStamp( tq1);
        } catch ( Exception s ){
            System.out.println( "Time tq: " + s.toString());
            s.printStackTrace();
        }

        try{
            tr = getTimeStamp( tr1 );
        } catch ( Exception s ){
            System.out.println( "Time tr: " + s.toString());
            s.printStackTrace();
        }

        // Get the domain from the recipient email address
        String localpart;
        if( r != null && r.length() > 0)
        {
            java.util.StringTokenizer st = new java.util.StringTokenizer( r, "@" );
            try{
                localpart = st.nextToken();
            }
            catch( Exception s ){
                System.out.println( "Token: " + s.toString());
                s.printStackTrace();
            }

            try{
                domain = st.nextToken();
            }
            catch( Exception s ){
                System.out.println( "Token: " + s.toString());
                s.printStackTrace();
            }
        }

        // Remove comma's and single quotes from text fields -
        //        This would cause the import to the db to fail.
        if( dsnDiag != null ){
            String s1 = dsnDiag.replace( ',',' ');
            String s2 = s1.replace( '\'', ' ');
            dsnDiag = s2;
        }

        if( dsnAct != null ){
            String s3 = dsnAct.replace( ',',' ');
            String s4 = s3.replace( '\'', ' ');
            dsnAct = s4;
        }

        if( dsnSts != null ){
            String s5 = dsnSts.replace( ',',' ');
            String s6 = s5.replace( '\'', ' ');
            dsnSts = s6;
        }

        if( dsnMTA != null ){
            String s7 = dsnMTA.replace( ',',' ');
            String s8 = s7.replace( '\'', ' ');
            dsnMTA = s8;
        }

        // TJR - 10 Nov 08
        // Modification on request of Chris A.
        // Fix yahoo bounce codes ....
        // All bounces are coming back as other
        // We will assume that all yahoo's are the same
        //	i.e. yahoo.co.uk, yahoo.sg ... yahoo.com

        if( domain.indexOf( "yahoo") > -1 )
        {
            if( bncCat.equalsIgnoreCase( "other") )
            {
                // Scan the dsnDiag for ...
                if( dsnDiag.indexOf( "not listed" ) > -1 )
                {
                    bncCat = "spam-related";
                }
                else if( dsnDiag.indexOf( "temporarily deferred" ) > -1 )
                {
                    bncCat = "spam-related";
                }
                else if( dsnDiag.indexOf( "TS03" ) > - 1)
                {
                    bncCat = "spam-related";
                }
            }
        }

        // Decode the verp
        // sp core lib for decode verp
        // dont attempt to decode if its not a verp
        if( orig.startsWith("v-"))
        {
            try {
                Verp lverp;
                String v;

                v = orig;

                lverp = new Verp( v );
                msgid = lverp.getMailingId();
                recpid = lverp.getRecipientId();
                retry = lverp.getRetryAttempt();
                jobId = lverp.getJobId();
                reportId = lverp.getReportId();
            }
            catch( Exception s ) {
                System.out.println( "VERP: [" + orig + "]");
                s.printStackTrace();
            }
        }


        // Additional processing for bounces
        try{
            if( dsnAct.equals( "failed"))
            {
                envId = Integer.toString( setKeyWords( dsnDiag, bncCat));

                if( envId.equals("0"))
                {
                    envId = Integer.toString( setDomainInfo( domain, bncCat));
                }

                // special case for inactive mailboxes that are classified as 'other'
                if( bncCat.equals( "other"))
                {
                    if( dsnDiag.indexOf( "extended inactivity" ) > 0)
                    {
                        bncCat = "inactive-mailbox";
                    }
                }
            }
        }
        catch( Exception x ) {
            x.printStackTrace();
        }

        // output the record to the csv
        try{

            // if no file to write to create it
            if ( fp == null )
                createFile();

            // Since it is possible to create a file larger that allowed by the file system
            // only allow 1MM (or maxLinesPerFile)
            if( currentLine > maxLinesPerFile ){
                currentLine = 0;
                //build new file name
                closeFile();
                createFile();
            }

            if(ssID.equals("")){
                fp.write( 		t + "," +
                        tq + "," +
                        envId + "," +
                        jobId + "," +
                        reportId + "," +
                        dlvFrom + "," +
                        dlvTo + "," +
                        orig + "," +
                        nRcpt + "," +
                        r + "," +
                        tr + "," +
                        dsnAct + "," +
                        dsnSts + "," +
                        dsnMTA + "," +
                        dsnDiag + "," +
                        bncCat + "," +
                        mta + "," +
                        vmta + "," +
                        msgid + "," +
                        recpid + "," +
                        retry + "," +
                        domain + "," +
                        size + "\n");

                currentLine++;

                try{
                    fp.flush();
                }
                catch (IOException s2)
                {
                    System.out.println( "Error Flushing Buffer");
                    s2.printStackTrace();

                }
            }
            else{

                // if no file to write to create it
                if ( fp2 == null )
                    createFileB2B();

                // Since it is possible to create a file larger that allowed by the file system
                // only allow 1MM (or maxLinesPerFile)
                if( currentLineB2B > maxLinesPerFile ){
                    currentLineB2B = 0;
                    //build new file name
                    closeFileB2B();
                    createFileB2B();
                }

                fp2.write( 		t + "," +
                        tq + "," +
                        dlvFrom + "," +
                        dlvTo + "," +
                        orig + "," +
                        nRcpt + "," +
                        r.replace(',',';') + "," +
                        tr + "," +
                        dsnAct + "," +
                        dsnSts + "," +
                        dsnMTA + "," +
                        dsnDiag + "," +
                        bncCat + "," +
                        mta + "," +
                        vmta + "," +
                        domain + "," +
                        fromAddress.replace( ',',' ') + "," +
                        ssID + "\n");

                currentLineB2B++;

                try{
                    fp2.flush();
                }
                catch (IOException s2)
                {
                    System.out.println( "Error Flushing Buffer");
                    s2.printStackTrace();

                }
            }
        }
        catch ( IOException s )
        {
            System.out.println( "Unable to write to file Level:" + level);
            s.printStackTrace();
        }
        initRecord();
    }



    /**
     * Sample main to process some accounting files.
     *
     * @param args
     *      should contain the path to the accounting file to process and
     *      the IP address to filter on.
     */
    public static void main(String args[]) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Expected arguments: path to accounting file and path to output directory");
        }

        String inputFile = args[0];
        String outputDir = args[1];
        int maxRecordsPerFile = 0;

        // if no 3rd argument or invalid integer set it to 0
        if (args.length == 3) {
            try {
                maxRecordsPerFile = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                maxRecordsPerFile = 0;
            }
        }


        // if we don't receive a CSV file as input assume it is the binary equivalent
        // The binary files usually look like "acct.*" whereas the CSV equivalent
        // will always end in csv
        String extension = FilenameUtils.getExtension(inputFile);

        if (extension.equalsIgnoreCase("csv")) processAsCSV(inputFile, outputDir, maxRecordsPerFile);
        else processAsBinary(inputFile, outputDir, maxRecordsPerFile);
    }


    static void processAsCSV(String inputFile, String outputDir, int maxRecordsPerFile)
    {
        AccountingRecords accRecords = new AccountingRecords();
        log_processing accProc = new log_processing(outputDir);

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

                    //System.out.println("Binary : " + accRecords.convertCsvToBinary(field.getKey()) + ", CSV : " + field.getKey());

                    accProc.processField(accRecords.convertCsvToBinary(field.getKey()), null, field.getValue());
                }

                accProc.endRecord();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            accProc.closeFile();
            accProc.closeFileB2B();
        }
    }



    static void processAsBinary(String inputFile, String outputDir, int maxRecordsPerFile )
    {
        log_processing accProc = new log_processing(outputDir);
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
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            acc.close();
        }
    }
}
