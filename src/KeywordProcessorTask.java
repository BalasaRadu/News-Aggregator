import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class KeywordProcessorTask implements Callable<Void> {
    private final List<Article> articlesChunk;

    public KeywordProcessorTask(List<Article> articlesChunk) {
        this.articlesChunk = articlesChunk;
    }

    @Override
    public Void call() {
        for (Article article : articlesChunk) {
            if (!"english".equals(article.getLanguage())) {
                continue;
            }
            String text = article.getText();
            if (text == null) {
                continue;
            }

            Set<String> wordsInThisArticle = new HashSet<>();

            Arrays.stream(text.toLowerCase().split(" "))
                    .map(token -> token.replaceAll("[^a-z]", ""))
                    .filter(word -> !word.isEmpty())
                    .filter(word -> !GlobalData.linkingWords.contains(word))
                    .forEach(wordsInThisArticle::add);

            wordsInThisArticle.forEach(
                    word -> GlobalData.keywordCounts
                    .computeIfAbsent(word, k -> new AtomicInteger(0))
                    .incrementAndGet());
        }
        return null;
    }
}