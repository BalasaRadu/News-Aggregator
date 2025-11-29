import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;


public class GlobalData {
    public static ConcurrentLinkedQueue<Article> rawArticles = new ConcurrentLinkedQueue<>();

    public static ConcurrentHashMap<String, Integer> uuidCounts = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, Integer> titleCounts = new ConcurrentHashMap<>();

    public static ConcurrentLinkedQueue<Article> validArticles = new ConcurrentLinkedQueue<>();

    // Category task
    public static Set<String> validCategories = new HashSet<>();
    public static ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> categoryToUuids = new ConcurrentHashMap<>();

    // Language task
    public static Set<String> validLanguages = new HashSet<>();
    public static ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> languageToUuids = new ConcurrentHashMap<>();

    // Words task
    public static Set<String> linkingWords = new HashSet<>();
    public static ConcurrentHashMap<String, AtomicInteger> keywordCounts = new ConcurrentHashMap<>();
}