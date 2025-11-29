import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

public class Tema1 {

    private static List<Callable<Void>> getJsonParserTasks(String articlesInputPath) {
        List<Callable<Void>> tasks = new ArrayList<>();
        File inputsFile = new File(articlesInputPath);
        File parentDir = inputsFile.getParentFile();

        try {
            BufferedReader br = new BufferedReader(new FileReader(inputsFile));
            String line = br.readLine().trim();

            for (int i = 0; i < Integer.parseInt(line); i++) {
                String jsonRelativePath = br.readLine().trim();
                File jsonFile = new File(parentDir, jsonRelativePath);
                tasks.add(new JsonParserTask(jsonFile.getPath()));
            }
            br.close();
        } catch (Exception e) {
            System.out.println("Error in gettingJsonParserTasks.");
            e.printStackTrace();
        }

        return tasks;
    }

    private static void createAllArticleFile(List<Article> finalList) {
        Collections.sort(finalList);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("all_articles.txt"));
            for (Article a : finalList) {
                bw.write(a.getUuid() + " " + a.getPublished());
                bw.newLine();
            }
            bw.close();
        } catch (Exception e) {
            System.out.println("Error in createAllArticleFile.");
            e.printStackTrace();
        }
    }

    private static void loadAuxiliaryData(String auxInputPath) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(auxInputPath));
            File auxDir = new File(auxInputPath).getParentFile();

            br.readLine();

            String languagesFilePath = br.readLine();
            String categoriesFilePath = br.readLine();
            String linkingWordsFilePath = br.readLine();

            loadSetFromFile(auxDir, languagesFilePath, GlobalData.validLanguages);
            loadSetFromFile(auxDir, categoriesFilePath, GlobalData.validCategories);
            loadSetFromFile(auxDir, linkingWordsFilePath, GlobalData.linkingWords);

            br.close();
        } catch (Exception e) {
            System.err.println("Error reading inputs.txt");
            e.printStackTrace();
        }
    }

    private static void loadSetFromFile(File baseDir, String relativePath, Set<String> targetSet) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return;
        }

        File file = new File(baseDir, relativePath.trim());

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = br.readLine();
            if (line != null) {
                int count = Integer.parseInt(line.trim());
                for (int i = 0; i < count; i++) {
                    String item = br.readLine();
                    if (item != null) {
                        targetSet.add(item.trim());
                    }
                }
            }
            br.close();
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading: " + relativePath);
            e.printStackTrace();
        }
    }

    private static void invokeAll(ExecutorService tpe, List<Callable<Void>> tasks) {
        try {
            List<Future<Void>> results = tpe.invokeAll(tasks);
            for (Future<Void> result : results) {
                result.get();
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java Tema1 <numar_threads> <fisier_articole> <fisier_suplimentar>");
            return;
        }

        int numThreads = Integer.parseInt(args[0]);
        String articlesInputPath = args[1];
        String auxInputPath = args[2];

        loadAuxiliaryData(auxInputPath);

        ExecutorService tpe = Executors.newFixedThreadPool(numThreads);

        try {
            // reading articles
            List<Callable<Void>> processJsonTasks = getJsonParserTasks(articlesInputPath);
            invokeAll(tpe, processJsonTasks);

            System.out.println("Finished reading. Raw articles: " + GlobalData.rawArticles.size());

            // removing duplicates
            List<Article> finalList = new ArrayList<>();
            for (Article a : GlobalData.rawArticles) {
                if (GlobalData.uuidCounts.get(a.getUuid()) == 1 &&
                        GlobalData.titleCounts.get(a.getTitle()) == 1) {
                    finalList.add(a);
                }
            }

            // sorting by categories and languages
            List<Callable<Void>> readerTasks = new ArrayList<>();

            Function<Article, List<String>> languageExtractor = article -> {
                String lang = article.getLanguage();
                if (lang == null) {
                    return Collections.emptyList();
                }
                return Collections.singletonList(lang);
            };

            int totalArticles = finalList.size();
            int chunkSize = totalArticles / numThreads;
            if (chunkSize == 0) {
                chunkSize = 1;
            }

            for (int i = 0; i < totalArticles; i += chunkSize) {
                int end = Math.min(i + chunkSize, totalArticles);
                List<Article> chunk = finalList.subList(i, end);
                readerTasks.add(new GenericReaderTask(chunk, GlobalData.validCategories, GlobalData.categoryToUuids, Article::getCategories));
                readerTasks.add(new GenericReaderTask(chunk, GlobalData.validLanguages, GlobalData.languageToUuids, languageExtractor));
                readerTasks.add(new GenericReaderTask(
                        chunk,
                        null,
                        GlobalData.authorToUuids,
                        article -> Collections.singletonList(article.getAuthor())
                ));;
                readerTasks.add(new KeywordProcessorTask(chunk));
            }

            invokeAll(tpe, readerTasks);

            List<Callable<Void>> writingTasks = new ArrayList<>();
            for (var entry : GlobalData.categoryToUuids.entrySet()) {
                writingTasks.add(new GenericWriterTask(
                        entry.getKey(),
                        entry.getValue(),
                        name -> name.replace(",", "").replaceAll("\\s+", "_") + ".txt"));
            }

            for (var entry : GlobalData.languageToUuids.entrySet()) {
                writingTasks.add(new GenericWriterTask(
                        entry.getKey(),
                        entry.getValue(),
                        name -> name + ".txt"));
            }

            writingTasks.add(new KeywordWriterTask());

            invokeAll(tpe, writingTasks);

            // sorting by time
            createAllArticleFile(finalList);

            List<Callable<Void>> reportTask = Collections.singletonList(
                    new ReportWriterTask(finalList, GlobalData.rawArticles.size())
            );
            invokeAll(tpe, reportTask);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tpe.shutdown();
            try {
                if (!tpe.awaitTermination(1, TimeUnit.SECONDS)) {
                    tpe.shutdownNow();
                }
            } catch (InterruptedException e) {
                tpe.shutdownNow();
            }
        }
    }
}