import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Article implements Comparable<Article> {

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("title")
    private String title;

    @JsonProperty("author")
    private String author;

    @JsonProperty("url")
    private String url;

    @JsonProperty("text")
    private String text;

    @JsonProperty("published")
    private String published;

    @JsonProperty("language")
    private String language;

    @JsonProperty("categories")
    private List<String> categories;

    public Article() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    @Override
    public int compareTo(Article other) {
        int dateComparison = other.published.compareTo(this.published);

        if (dateComparison != 0) {
            return dateComparison;
        }

        return this.uuid.compareTo(other.uuid);
    }

    @Override
    public String toString() {
        return "Article{" +
                "uuid='" + uuid + '\'' +
                ", published='" + published + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}