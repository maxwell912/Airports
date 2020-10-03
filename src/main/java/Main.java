import airport.info.AirportApplication;
import org.apache.commons.cli.*;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        var context = new ClassPathXmlApplicationContext(
                "applicationContext.xml"
        );

        var parsedArgs = getArgs(args);
        var filename = (String)parsedArgs.get("filename");
        var column = Integer.parseInt((String) parsedArgs.get("column"));

        var app = context.getBean(AirportApplication.class);
        try {
            app.init(filename, column);
            app.run();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    static Map<String, Object> getArgs(String[] args) {
        Map<String, Object> parsedArgs = null;

        if (args.length > 1)
            try {
                parsedArgs = parseCl(args);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        if (parsedArgs == null)
            try {
                parsedArgs = parseYaml("appConf.yaml");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        if (parsedArgs == null) {
            System.out.println("No filename and column arguments available");
            System.exit(1);
        }

        return parsedArgs;
    }

    static Map<String, Object> parseYaml(String yamlFile) throws FileNotFoundException {
        Yaml yaml = new Yaml();
        var yamlReader = new FileInputStream(yamlFile);
        return yaml.load(yamlReader);
    }

    static Map<String, Object> parseCl(String[] args) throws ParseException {
        Options options = new Options();

        Option input = new Option(
                "f", "filename", true, "Data filename"
        );
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option(
                "c", "column", true, "Search column index"
        );
        output.setRequired(true);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        var cmd = parser.parse(options, args);
        var result = new HashMap<String, Object>();
        result.put("filename", cmd.getOptionValue("filename"));
        result.put("column", cmd.getOptionValue("column"));
        return result;
    }
}
