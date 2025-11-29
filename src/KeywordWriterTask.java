import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class KeywordWriterTask implements Callable<Void> {

    @Override
    public Void call() throws Exception {
        List<Map.Entry<String, AtomicInteger>> sortedList = getEntries();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("keywords_count.txt"))) {
            for (Map.Entry<String, AtomicInteger> entry : sortedList) {
                bw.write(entry.getKey() + " " + entry.getValue().get());
                bw.newLine();
            }
        } catch (Exception e) {
            System.err.println("Error in  KeywordWriterTask");
            e.printStackTrace();
        }

        return null;
    }

    private static List<Map.Entry<String, AtomicInteger>> getEntries() {
        List<Map.Entry<String, AtomicInteger>> sortedList = new ArrayList<>(GlobalData.keywordCounts.entrySet());

        sortedList.sort((e1, e2) -> {
            int count1 = e1.getValue().get();
            int count2 = e2.getValue().get();

            if (count1 != count2) {
                return count2 - count1;
            }
            return e1.getKey().compareTo(e2.getKey());
        });
        return sortedList;
    }
}