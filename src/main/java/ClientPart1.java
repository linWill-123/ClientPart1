import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.DefaultApi;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientPart1 {
  private static AtomicInteger success = new AtomicInteger(0);
  private static AtomicInteger failure = new AtomicInteger(0);
  public ClientPart1() { }

  private static long runThreads(final String basePath, int threadGroupSize, int numThreadGroups,
      int delaySeconds) throws InterruptedException{
    ExecutorService executorService = Executors.newFixedThreadPool(threadGroupSize);

    for (int i = 0; i < 10; i++) {
      submitTask(basePath, executorService, 100);
    }

    executorService.shutdown();
    executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    executorService = Executors.newFixedThreadPool(threadGroupSize);

    long startTime = System.currentTimeMillis();

    for (int group = 0; group < numThreadGroups; group++) {
      for (int i = 0; i < threadGroupSize; i++) {
        submitTask(basePath, executorService, 1000);
      }
      Thread.sleep(delaySeconds * 1000);
    }

    executorService.shutdown();
    executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);

    long endTime = System.currentTimeMillis();
    long wallTime = (endTime - startTime) / 1000;
    long throughput = success.get() / wallTime;

    System.out.println("Wall Time: " + wallTime + " seconds");
    System.out.println("Throughput: " + throughput + " requests/second");
    System.out.println("Successful Requests: " + success);
    System.out.println("Failed Requests: " + failure);

    return throughput;
  }

  private static void submitTask(final String basePath, ExecutorService executorService, final int loopCount) {
    executorService.submit(new Runnable() {
      @Override
      public void run() {
        ApiClient client = new ApiClient();
        client.setBasePath(basePath);
        DefaultApi apiInstance = new DefaultApi(client);

        int localSuccess = 0;
        int localFailure = 0;

        for (int i = 0; i < loopCount; i++) {
          File image = new File("/Users/willxzy/Downloads/Assignment1.Problem1.jpg");
          AlbumsProfile profile = new AlbumsProfile();
          profile.setArtist("Eminem");
          profile.setTitle("MMlp2");
          profile.setYear("2001");

          try {
            ImageMetaData postResponse = apiInstance.newAlbum(image, profile);
            String albumID = postResponse.getAlbumID();
            AlbumInfo getResponse = apiInstance.getAlbumByKey(albumID);
            localSuccess++;
          } catch (ApiException e) {
            localFailure++;
            System.err.println("Exception when calling DefaultApi");
            e.printStackTrace();
          }
        }

        success.addAndGet(localSuccess);
        failure.addAndGet(localFailure);
      }
    });
  }

//  private static void appendToCSV(String filename, String configuration, long wallTime, long throughput) {
//    try (FileWriter fw = new FileWriter(filename, true);
//        BufferedWriter bw = new BufferedWriter(fw);
//        PrintWriter out = new PrintWriter(bw)) {
//      out.println(configuration + "," + wallTime + "," + throughput);
//    } catch (IOException e) {
//      System.err.println("Error writing to CSV file: " + e.getMessage());
//    }
//  }

  public static void main(String[] args) throws InterruptedException {
    int threadGroupSize;
    int numThreadGroups;
    int delaySeconds;

    String baseUrl = "http://localhost:8080/AlbumServlet_war_exploded";
    String baseUrlGo = "http://localhost:8080";

    threadGroupSize = 10;
    numThreadGroups = 10;
    delaySeconds = 2;
    long test1Throughput = runThreads(baseUrlGo,threadGroupSize,numThreadGroups,delaySeconds);

//    threadGroupSize = 10;
//    numThreadGroups = 20;
//    delaySeconds = 2;
//    long test2Throughput = runThreads(baseUrl,threadGroupSize,numThreadGroups,delaySeconds);
//
//    threadGroupSize = 10;
//    numThreadGroups = 30;
//    delaySeconds = 2;
//    runThreads(baseUrl,threadGroupSize,numThreadGroups,delaySeconds);
//    long test3Throughput = runThreads(baseUrl,threadGroupSize,numThreadGroups,delaySeconds);


  }
}
