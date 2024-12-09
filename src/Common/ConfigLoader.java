package Common;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigLoader {
    public static Properties loadConfig(String path) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("C:/Users/guipeliceri/IdeaProjects/EXAMEPOO2//src/config.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("Erro ao carregar o arquivo de configuração: " + e.getMessage());
        }
        return properties;
    }
}

