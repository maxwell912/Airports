package airport.info;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Scanner;

@Component
public class AirportApplication {

    @Autowired
    AirportInfo airportInfo;
    boolean isInitialized = false;

    public void init(String filename, Integer column) throws IOException {
        airportInfo.init(filename, column);
        isInitialized = true;
    }

    public void run() throws Exception {
        if (!isInitialized)
            throw new Exception("Application was not initialized");

        var scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Write prefix to search (or \"exit\"): ");
            var prefix = scanner.nextLine();

            if (prefix.equals("exit")) {
                break;
            }
            long start = System.currentTimeMillis();
            var count = 0;

            for (var info : airportInfo.getByPrefix(prefix)) {
                count++;
                System.out.println(String.join(", ", info));
            }
            long finish = System.currentTimeMillis();

            System.out.println("Airports count: " + count);
            System.out.println("Time: " + (finish - start) + " ms");
        }
    }
}
