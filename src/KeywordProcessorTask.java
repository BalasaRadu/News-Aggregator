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
            if ("english".equals(article.getLanguage())) {
                String text = article.getText();
                if (text == null) continue;

                Set<String> wordsInThisArticle = new HashSet<>();

                String[] tokens = text.toLowerCase().split("[\\s]+");

                for (String token : tokens) {
                    String cleanWord = token.replaceAll("[^a-z]", "");

                    if (!cleanWord.isEmpty() && !GlobalData.linkingWords.contains(cleanWord)) {
                        wordsInThisArticle.add(cleanWord);
                    }
                }

                for (String word : wordsInThisArticle) {
                    GlobalData.keywordCounts
                            .computeIfAbsent(word, k -> new AtomicInteger(0))
                            .incrementAndGet();
                }
            }
        }
        return null;
    }
}