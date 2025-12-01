import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class FilterTask implements Callable<List<Article>> {
    private final List<Article> rawChunk;

    public FilterTask(List<Article> rawChunk) {
        this.rawChunk = rawChunk;
    }

    @Override
    public List<Article> call() {
        List<Article> localValid = new ArrayList<>();
        for (Article a : rawChunk) {
            if (GlobalData.uuidCounts.get(a.getUuid()) == 1 &&
                    GlobalData.titleCounts.get(a.getTitle()) == 1) {
                localValid.add(a);
            }
        }
        return localValid;
    }
}