import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.concurrent.Callable;

public class JsonParserTask implements Callable<Void> {
    private final String filePath;
    private final ObjectMapper objectMapper;

    public JsonParserTask(String filePath) {
        this.filePath = filePath;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Void call() throws Exception {
        System.out.println(filePath);
        File file = new File(filePath);
        Article[] articles = objectMapper.readValue(file, Article[].class);

        for (Article article : articles) {
            GlobalData.rawArticles.add(article);
            GlobalData.uuidCounts.merge(article.getUuid(), 1, Integer::sum);
            GlobalData.titleCounts.merge(article.getTitle(), 1, Integer::sum);
        }

        return null;
    }
}