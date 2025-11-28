import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class GlobalData {
    public static ConcurrentLinkedQueue<Article> rawArticles = new ConcurrentLinkedQueue<>();

    public static ConcurrentHashMap<String, Integer> uuidCounts = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Integer> titleCounts = new ConcurrentHashMap<>();

    public static ConcurrentLinkedQueue<Article> validArticles = new ConcurrentLinkedQueue<>();
}