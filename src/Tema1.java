import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Tema1 {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Tema1 <numar_threads> <fisier_articole> <fisier_suplimentar>");
            return;
        }

        int numThreads = Integer.parseInt(args[0]);
        String articlesInputPath = args[1];

        ExecutorService tpe = Executors.newFixedThreadPool(numThreads);

        try {
            // --- ETAPA 1: CITIRE (PARALELĂ) ---
            List<Future<?>> parseFutures = new ArrayList<>();

            File inputsFile = new File(articlesInputPath);
            File parentDir = inputsFile.getParentFile();

            try (BufferedReader br = new BufferedReader(new FileReader(inputsFile))) {
                String line = br.readLine(); // Citim numărul de fișiere (dacă există)

                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;

                    String jsonRelativePath = line.trim();

                    // 2. Combinăm directorul părinte cu calea relativă din fișier
                    // Rezultatul va fi calea corectă către JSON
                    File jsonFile = new File(parentDir, jsonRelativePath);

                    // Trimitem calea completă/rezolvată către Task
                    JsonParserTask task = new JsonParserTask(jsonFile.getPath());
                    parseFutures.add(tpe.submit(task));
                }
            }

            // BARIERĂ: Așteptăm să se termine toate citirile
            for (Future<?> future : parseFutures) {
                future.get(); // Blochează până când task-ul e gata
            }
            // În acest punct, GlobalData.rawArticles conține TOT, iar contoarele sunt populate.

            // --- ETAPA 2: FILTRARE DUPLICATE (PARALELIZABILĂ sau SECVENȚIALĂ) ---
            // Pentru simplitate și corectitudine maximă la început, o facem pe main thread sau un singur worker.
            // Dacă setul de date e imens, se poate paraleliza ușor.

            List<Article> finalList = new ArrayList<>();
            for (Article a : GlobalData.rawArticles) {
                // Verificăm condiția: Uuid Count == 1 ȘI Title Count == 1
                // Dacă un articol apare de 2 ori, count va fi 2, deci îl excludem pe ambele.
                if (GlobalData.uuidCounts.get(a.getUuid()) == 1 &&
                        GlobalData.titleCounts.get(a.getTitle()) == 1) {
                    finalList.add(a);
                }
            }

            // --- ETAPA 3: SORTARE (SECVENȚIALĂ) ---
            // Sortăm folosind compareTo-ul definit în clasa Article
            // (Cronologic descrescător, apoi UUID crescător)
            Collections.sort(finalList);

            // --- ETAPA 4: SCRIERE (SECVENȚIALĂ PENTRU ACEST FIȘIER) ---
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("all_articles.txt"))) {
                for (Article a : finalList) {
                    // Formatul cerut: <uuid> <published>
                    bw.write(a.getUuid() + " " + a.getPublished());
                    bw.newLine();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Închidem pool-ul corect
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