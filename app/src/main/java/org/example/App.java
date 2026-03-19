package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App extends Application {

    // ── Configuration ─────────────────────────────────────────────────────────
    private static final String TICKER      = "%5EDJI";
    private static final int    INTERVAL_MS = 5_000;
    private static final int    MAX_ENTRIES = 100;

    private static final String URL = "https://query1.finance.yahoo.com"
            + "/v8/finance/chart/" + TICKER + "?interval=1m&range=1d";

    private static final Pattern PRICE_PATTERN =
            Pattern.compile("\"regularMarketPrice\":(\\d+\\.?\\d*)");

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    // ── Shared state ──────────────────────────────────────────────────────────
    private final Queue<StockRecord> stockQueue = new LinkedList<>();
    private XYChart.Series<Number, Number> series;
    private int tickCount = 0;

    // ── Data record ───────────────────────────────────────────────────────────
    static class StockRecord {
        final int        tick;
        final String     timestamp;
        final BigDecimal price;

        StockRecord(int tick, String timestamp, BigDecimal price) {
            this.tick      = tick;
            this.timestamp = timestamp;
            this.price     = price;
        }
    }

    // ── JavaFX entry point ────────────────────────────────────────────────────
    @Override
    public void start(Stage stage) {

        // X axis — tick number
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Query #");
        xAxis.setAutoRanging(true);
        xAxis.setForceZeroInRange(false);

        // Y axis — price
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("DJIA Price (USD)");
        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(false);
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, "$", null));

        // Line chart
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Dow Jones Industrial Average — Live Feed");
        chart.setAnimated(false);
        chart.setCreateSymbols(true);

        series = new XYChart.Series<>();
        series.setName("^DJI");
        chart.getData().add(series);

        Scene scene = new Scene(chart, 900, 500);
        stage.setTitle("Citi DJIA Monitor");
        stage.setScene(scene);
        stage.show();

        // Start polling on a background thread so UI stays responsive
        Thread poller = new Thread(this::pollLoop, "djia-poller");
        poller.setDaemon(true);   // stops automatically when the window closes
        poller.start();
    }

    // ── Polling loop (background thread) ─────────────────────────────────────
    private void pollLoop() {
        System.out.println("=".repeat(54));
        System.out.println("  Citi DJIA Real-Time Monitor");
        System.out.println("  Ticker  : ^DJI");
        System.out.printf ("  Polling : every %ds  |  Queue cap: %d%n",
                INTERVAL_MS / 1000, MAX_ENTRIES);
        System.out.println("  Press Ctrl+C or close the window to stop.");
        System.out.println("=".repeat(54));
        System.out.printf("%n%-5s  %-12s  %15s  %10s%n",
                "#", "Time", "DJIA Price", "Queue Size");
        System.out.println("-".repeat(48));

        while (!Thread.currentThread().isInterrupted()) {
            BigDecimal price = fetchPrice();

            if (price != null) {
                tickCount++;
                String timestamp = LocalDateTime.now().format(FORMATTER);
                StockRecord record = new StockRecord(tickCount, timestamp, price);

                // Maintain bounded queue
                if (stockQueue.size() >= MAX_ENTRIES) {
                    stockQueue.poll();
                }
                stockQueue.offer(record);

                System.out.printf("%-5d  %-12s  $%,14.2f  %10d%n",
                        tickCount, timestamp, price, stockQueue.size());

                // Update the chart on the JavaFX application thread
                Platform.runLater(() -> updateChart());
            } else {
                System.out.printf("  [Warning] No price retrieved. Retrying in %ds...%n",
                        INTERVAL_MS / 1000);
            }

            try {
                Thread.sleep(INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // ── Chart update (must run on JavaFX thread via Platform.runLater) ────────
    private void updateChart() {
        series.getData().clear();
        for (StockRecord r : stockQueue) {
            series.getData().add(
                new XYChart.Data<>(r.tick, r.price.doubleValue())
            );
        }
    }

    // ── HTTP fetch ────────────────────────────────────────────────────────────
    public static BigDecimal fetchPrice() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                            + "AppleWebKit/537.36 (KHTML, like Gecko) "
                            + "Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response =
                    HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Matcher matcher = PRICE_PATTERN.matcher(response.body());
                if (matcher.find()) {
                    return new BigDecimal(matcher.group(1));
                }
            } else {
                System.out.println("  [Error] HTTP " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("  [Error] " + e.getMessage());
        }
        return null;
    }

    // ── Main ──────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        launch(args);
    }
}
