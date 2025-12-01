import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class AllArticlesWriterTask implements Callable<Void> {
    private final List<Article> articles;

    public AllArticlesWriterTask(List<Article> articles) {
        this.articles = articles;
    }

    @Override
    public Void call() throws Exception {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("all_articles.txt"))) {
            for (Article a : articles) {
                bw.write(a.getUuid() + " " + a.getPublished());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing in all_articles.txt");
            e.printStackTrace();
        }
        return null;
    }
}