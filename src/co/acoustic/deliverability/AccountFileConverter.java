package co.acoustic.deliverability;

public interface AccountFileConverter {
    public int processFile(String inputFile, String outputDir, int maxRecordsPerFile);
}
