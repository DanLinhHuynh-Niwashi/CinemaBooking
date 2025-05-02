package com.example.cinemabooking.DAO;

import com.example.cinemabooking.Model.ShowTime;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class ShowTimeDAOTest {

    private Connection connection;
    private ShowTimeDAO showTimeDAO;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        Statement stmt = connection.createStatement();
        stmt.execute("CREATE TABLE showtimes (" +
                "showTimeId INT AUTO_INCREMENT PRIMARY KEY, " +
                "movieId INT, roomId INT, startTime TIMESTAMP, ticketPrice DOUBLE)");
        showTimeDAO = new ShowTimeDAO(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }
    @Test
    void testConcurrentDuplicateShowTimeInsertion() throws InterruptedException {
        int threadCount = 10;
        int insertsPerThread = 9;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        List<Future<List<Integer>>> results = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            results.add(executor.submit(() -> {
                List<ShowTime> showTimes = new ArrayList<>();
                List<Integer> insertedIds = new ArrayList<>();
                // Create 9 showtimes at different times (all in room 1 and movie 1)
                for (int j = 0; j < insertsPerThread; j++) {
                    showTimes.add(new ShowTime(
                            0,
                            1,
                            1,
                            LocalDateTime.of(2025, 5, 2, 20, 0).plusHours(j),
                            10.0 + j
                    ));
                }
                Collections.shuffle(showTimes);

                for (ShowTime st : showTimes) {
                    int result = showTimeDAO.addShowTime(st);
                    if (result != -1) {
                        insertedIds.add(result);
                        System.out.println("Thread " + threadId + " inserted ShowTime at " + st.getStartTime() + " with ID: " + result);
                    } else {
                        System.out.println("Thread " + threadId + " failed to insert ShowTime at " + st.getStartTime());
                    }
                }
                return insertedIds;
            }));
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        int totalInserted = 0;
        for (Future<List<Integer>> result : results) {
            try {
                totalInserted += result.get().size();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Total inserted showtimes: " + totalInserted);
        assertTrue(totalInserted <= insertsPerThread, "No duplicate showtimes at the same time should be inserted.");
    }


}
