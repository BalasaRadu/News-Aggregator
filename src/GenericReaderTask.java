import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class GenericReaderTask implements Callable<Void> {
    private final List<Article> articlesChunk;
    private final Set<String> validValues;
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> targetMap;
    private final Function<Article, List<String>> fieldExtractor;

    public GenericReaderTask(List<Article> articlesChunk,
                             Set<String> validValues,
                             ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> targetMap,
                             Function<Article, List<String>> fieldExtractor) {
        this.articlesChunk = articlesChunk;
        this.validValues = validValues;
        this.targetMap = targetMap;
        this.fieldExtractor = fieldExtractor;
    }

    public Void call() {
        for (Article article:articlesChunk) {
            List<String> field = fieldExtractor.apply(article);
            if (field == null || field.isEmpty()) {
                continue;
            }
            for (String val : field) {
                if (validValues.contains(val)) {
                    targetMap.computeIfAbsent(val, _ -> new ConcurrentLinkedQueue<>())
                            .add(article.getUuid());
                }
            }
        }
        return null;
    }
}
