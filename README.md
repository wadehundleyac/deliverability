# deliverability

Contains the log processor which is used to convert the PMTA logs to CSV files which are consumed by kshell scripts and loaded into the deliverability DB.

PmtaAccountingFileProcessor.java is a modified version of the original which was developed several years ago. Most of the logic for interpreting fields is being left as is. The updates focused on adding support for CSV version of the PMTA accounting files. Now the application will consume either the original Binary form of the accounting files or the new CSV versions.

The Main class was moved to ApplicationMain.  The logic for reading Binary files was moved to BinaryAccountFileConverter.  The logic for reading CSV files was moved to CsvAccountFileConverter.

AccountRecords class was added to convert the newer field names found in the CSV files to the older style field names so that the rest of the logic in log_processing can parse the data without changes.

The data directory contains the binary form of accounting data (acct.40) and the csv version of the data (acct-***.csv)

The build.xml and ivy.xml files were created to support an ANT build. However, the output jar is not currently runnable due to a class not found exception. Will work on this more later; however, there is a valid intellij project that can be used for building the jar.

When starting the jar you have to specify the java.library.path to the dist directory which contains some native library which the PMTA jar attempts to load dynamically.  If this is not done then you will get a LoadLibrary exception when attempting to convert a binary file.  The CSV conversion does not require it.

