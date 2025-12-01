import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ArticleProcessingTask implements Callable<Void> {
    private final List<Article> articlesChunk;

    public ArticleProcessingTask(List<Article> articlesChunk) {
        this.articlesChunk = articlesChunk;
    }

    @Override
    public Void call() {
        for (Article article : articlesChunk) {
            String uuid = article.getUuid();

            if (article.getCategories() != null) {
                Set<String> uniqueCats = new HashSet<>(article.getCategories());
                for (String cat : uniqueCats) {
                    if (GlobalData.validCategories.contains(cat)) {
                        GlobalData.categoryToUuids
                                .computeIfAbsent(cat, k -> new ConcurrentLinkedQueue<>())
                                .add(uuid);
                    }
                }
            }

            String lang = article.getLanguage();
            if (lang != null && GlobalData.validLanguages.contains(lang)) {
                GlobalData.languageToUuids
                        .computeIfAbsent(lang, k -> new ConcurrentLinkedQueue<>())
                        .add(uuid);
            }

            String author = article.getAuthor();
            if (author != null) {
                GlobalData.authorToUuids
                        .computeIfAbsent(author, k -> new ConcurrentLinkedQueue<>())
                        .add(uuid);
            }

            if ("english".equals(lang)) {
                processKeywords(article);
            }
        }
        return null;
    }

    private void processKeywords(Article article) {
        String text = article.getText();
        if (text == null) return;

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