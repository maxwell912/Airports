package airport.info;

import au.com.bytecode.opencsv.CSVParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Iterator;

@Component
class AirportInfo {

    @Autowired
    PrefixMap<String, Long> airportIndexes;
    String filename;

    AirportInfo() {}

    public void init(String dataFilename, int searchColumnIdx) throws IOException {
        filename = dataFilename;

        RandomAccessFile file = getFile(filename);
        var parser = new CSVParser(',','"');

        String[] aiportInfo;
        long lineOffset;
        String newLine;
        while (true) {
            try {
                lineOffset = file.getFilePointer();
                newLine = file.readLine();
            } catch (IOException e) {
                return;
            }
            aiportInfo = parser.parseLine(newLine);
            if (aiportInfo == null)
                break;
            if (searchColumnIdx >= 0 && searchColumnIdx < aiportInfo.length)
                airportIndexes.add(aiportInfo[searchColumnIdx], lineOffset);
        }
    }

    public Iterable<String[]> getByPrefix(String prefix) {
        return new AirportInfoIterator(
                airportIndexes.getByPrefix(prefix).iterator(),
                getFile(filename)
        );
    }

    RandomAccessFile getFile(String filename) {
        try {
            return new RandomAccessFile(filename, "r");
        } catch (FileNotFoundException e) {
            System.out.println("Missing file: " + filename);
            System.exit(1);
            return null;
        }
    }

    static class AirportInfoIterator implements Iterable<String[]>, Iterator<String[]> {

        Iterator<Long> indexIterator;
        RandomAccessFile reader;

        CSVParser parser = new CSVParser();

        AirportInfoIterator(Iterator<Long> indexIterator, RandomAccessFile reader) {
            this.indexIterator = indexIterator;
            this.reader = reader;
        }

        @Override
        public boolean hasNext() {
            return indexIterator.hasNext();
        }

        @Override
        public String[] next() {
            try {
                reader.seek(indexIterator.next());
                return parser.parseLine(reader.readLine());
            } catch (IOException e) {
                return new String[0];
            }
        }

        @Override
        public Iterator<String[]> iterator() {
            return this;
        }
    }
}


