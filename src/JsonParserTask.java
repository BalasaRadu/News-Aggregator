import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class JsonParserTask implements Runnable {
    private final String filePath;
    private final ObjectMapper objectMapper;

    public JsonParserTask(String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void run() {
        try {
            File file = new File(filePath);
            Article[] articles = objectMapper.readValue(file, Article[].class);

            for (Article article : articles) {
                GlobalData.rawArticles.add(article);
                GlobalData.uuidCounts.merge(article.getUuid(), 1, Integer::sum);
                GlobalData.titleCounts.merge(article.getTitle(), 1, Integer::sum);
            }

        } catch (IOException e) {
            System.err.println("Eroare la citirea fisierului: " + filePath);
            e.printStackTrace();
        }
    }
}