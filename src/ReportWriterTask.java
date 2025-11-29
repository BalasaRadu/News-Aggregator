import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ReportWriterTask implements Callable<Void> {
    private final List<Article> validArticles;
    private final int totalRawArticles;

    public ReportWriterTask(List<Article> validArticles, int totalRawArticles) {
        this.validArticles = validArticles;
        this.totalRawArticles = totalRawArticles;
    }

    private Map.Entry<String, Integer> getTopEntry(Map<String, ?> map) {
        String bestKey = "";
        int maxCount = -1;

        for (var entry : map.entrySet()) {
            String key = entry.getKey();
            int count;

            Object value = entry.getValue();
            if (value instanceof ConcurrentLinkedQueue) {
                count = ((ConcurrentLinkedQueue<?>) value).size();
            } else if (value instanceof AtomicInteger) {
                count = ((AtomicInteger) value).get();
            } else {
                continue;
            }

            if (count > maxCount) {
                maxCount = count;
                bestKey = key;
            } else if (count == maxCount) {
                if (bestKey.isEmpty() || key.compareTo(bestKey) < 0) {
                    bestKey = key;
                }
            }
        }

        return Map.entry(bestKey, maxCount);
    }

    @Override
    public Void call() throws Exception {
        int uniqueCount = validArticles.size();
        int duplicates = totalRawArticles - uniqueCount;

        Map.Entry<String, Integer> bestAuthorEntry = getTopEntry(GlobalData.authorToUuids);
        Map.Entry<String, Integer> topLanguageEntry = getTopEntry(GlobalData.languageToUuids);
        Map.Entry<String, Integer> topKeywordEntry = getTopEntry(GlobalData.keywordCounts);

        String topCategory = "";
        int maxCatCount = -1;
        for (var entry : GlobalData.categoryToUuids.entrySet()) {
            String cat = entry.getKey();
            int count = entry.getValue().size();
            String normalizedCat = cat.replace(",", "").replaceAll("\\s+", "_");

            if (count > maxCatCount) {
                maxCatCount = count;
                topCategory = normalizedCat;
            } else if (count == maxCatCount) {
                if (topCategory.isEmpty() || normalizedCat.compareTo(topCategory) < 0) {
                    topCategory = normalizedCat;
                }
            }
        }

        Article mostRecent = validArticles.isEmpty() ? null : validArticles.getFirst();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("reports.txt"))) {
            bw.write("duplicates_found - " + duplicates);
            bw.newLine();

            bw.write("unique_articles - " + uniqueCount);
            bw.newLine();

            bw.write("best_author - " + bestAuthorEntry.getKey() + " " + bestAuthorEntry.getValue());
            bw.newLine();

            bw.write("top_language - " + topLanguageEntry.getKey() + " " + topLanguageEntry.getValue());
            bw.newLine();

            bw.write("top_category - " + topCategory + " " + maxCatCount);
            bw.newLine();

            if (mostRecent != null) {
                bw.write("most_recent_article - " + mostRecent.getPublished() + " " + mostRecent.getUrl());
            } else {
                bw.write("most_recent_article - null null");
            }
            bw.newLine();

            bw.write("top_keyword_en - " + topKeywordEntry.getKey() + " " + topKeywordEntry.getValue());
            bw.newLine();
        }

        return null;
    }
}