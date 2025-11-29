import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class GenericWriterTask implements Callable<Void> {
    private String fieldName;
    private final ConcurrentLinkedQueue<String> uuids;
    private final Function<String, String> fileNameGenerator;

    GenericWriterTask(String fieldName, ConcurrentLinkedQueue<String> uuids, Function<String, String> fileNameGenerator) {
        this.fieldName = fieldName;
        this.uuids = uuids;
        this.fileNameGenerator = fileNameGenerator;
    }

    @Override
    public Void call() throws Exception {
        List<String> sortedUuids = new ArrayList<>(uuids);
        Collections.sort(sortedUuids);
        String fileName = fileNameGenerator.apply(fieldName);
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
            for (String uuid : sortedUuids) {
                bw.write(uuid);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            System.err.println("Error in GenericWriterTask: " + fileName);
            e.printStackTrace();
        }
        return null;
    }
}
