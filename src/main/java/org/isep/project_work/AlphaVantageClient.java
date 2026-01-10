package org.isep.project_work;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AlphaVantageClient {

    private static final String API_KEY = "SETQ8K4QV0MMU05B";


    public static double getStockPrice(String symbol) throws Exception {
        String urlStr ="https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol="+symbol + "&apikey="+API_KEY;
        URL url = new URL(urlStr);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        System.out.println(response);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.toString());

        JsonNode quote = root.get("Global Quote");
        if (quote == null || quote.isEmpty()) {
            throw new RuntimeException("No data returned !");
        }

        return quote.get("05. price").asDouble();
    }

    public static void main(String[] args) throws Exception {
     double price = getStockPrice("AAPL");
     System.out.println(("AAPL Price: " + price));
    }
}