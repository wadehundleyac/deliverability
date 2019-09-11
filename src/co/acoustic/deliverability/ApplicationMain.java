package co.acoustic.deliverability;

import org.apache.commons.io.FilenameUtils;

public class ApplicationMain {

    /**
     * Sample main to process some accounting files.
     *
     * @param args
     *      should contain the path to the accounting file to process and
     *      the output directory for creating the CSV files.
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Expected arguments: path to accounting file and path to output directory");
        }

        String inputFile = args[0];
        String outputDir = args[1];
        int maxRecordsPerFile = 0;

        // if no 3rd argument or invalid integer set maxRecords to 0
        // log_processing will generate one file per maxRecord count using the
        // naming convention *.0.csv, *.1.csv, etc
        if (args.length == 3) {
            try {
                maxRecordsPerFile = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                maxRecordsPerFile = 0;
            }
        }


        // if we don't receive a CSV file as input assume it is the binary equivalent
        // The binary files usually look like "acct.*" (e.g. acct.40) whereas the CSV equivalent
        // will always end in csv
        String extension = FilenameUtils.getExtension(inputFile);
        int recordsProcessed=0;

        AccountFileConverter fileConverter;
        if (extension.equalsIgnoreCase("csv")) {
            fileConverter = new CsvAccountFileConverter();
        }
        else {
            fileConverter = new BinaryAccountFileConverter();
        }
        recordsProcessed = fileConverter.processFile(inputFile, outputDir, maxRecordsPerFile);

        // used during performance testing only
        System.out.println("Record Count" + recordsProcessed);
    }

}
