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

    public static void main(String[] args) {
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
            tpe.invokeAll(processJsonTasks);

            // removing duplicates
            List<Article> rawList = new ArrayList<>(GlobalData.rawArticles);

            List<Callable<List<Article>>> filterTasks = new ArrayList<>();
            int totalRaw = rawList.size();
            int filterChunkSize = Math.max(1, totalRaw / numThreads);

            for (int i = 0; i < totalRaw; i += filterChunkSize) {
                int end = Math.min(i + filterChunkSize, totalRaw);
                filterTasks.add(new FilterTask(rawList.subList(i, end)));
            }

            List<Future<List<Article>>> filterResults = tpe.invokeAll(filterTasks);

            List<Article> finalList = new ArrayList<>();
            for (Future<List<Article>> res : filterResults) {
                finalList.addAll(res.get());
            }

            // processing the articles
            List<Callable<Void>> processingTasks = new ArrayList<>();

            int totalArticles = finalList.size();
            int chunkSize = Math.max(1, finalList.size() / numThreads);

            for (int i = 0; i < totalArticles; i += chunkSize) {
                int end = Math.min(i + chunkSize, totalArticles);
                List<Article> chunk = finalList.subList(i, end);
                processingTasks.add(new ArticleProcessingTask(chunk));
            }

            tpe.invokeAll(processingTasks);

            // sorting by time
            Collections.sort(finalList);

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


            writingTasks.add(new AllArticlesWriterTask(finalList));
            writingTasks.add(new KeywordWriterTask());
            writingTasks.add(new ReportWriterTask(finalList, GlobalData.rawArticles.size()));

            tpe.invokeAll(writingTasks);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            tpe.shutdown();
        }
    }
}