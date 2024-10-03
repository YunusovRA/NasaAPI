package ru.netology;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

public class NasaImageDownloader {

    public static void main(String[] args) {
        String apiKey = "K0Kp3tZXlVFZjDpDfOeczRlzFzkizw2ODATkstNo";

        CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)    // Максимальное время ожидание подключения к серверу
                        .setSocketTimeout(30000)    // Максимальное время ожидания получения данных
                        .setRedirectsEnabled(false) // Возможность следовать редиректу в ответе
                        .build())
                .build();

        try {
            String nasaApiUrl = "https://api.nasa.gov/planetary/apod?api_key=" + apiKey;

            HttpGet request = new HttpGet(nasaApiUrl);

            CloseableHttpResponse response = httpClient.execute(request);

            try {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    System.out.println("Ошибка при получении данных: код ответа " + statusCode);
                    return;
                }

                HttpEntity entity = response.getEntity();
                String jsonResponse = EntityUtils.toString(entity);

                ObjectMapper mapper = new ObjectMapper();
                NasaResponse nasaResponse = mapper.readValue(jsonResponse, NasaResponse.class);

                String imageUrl = nasaResponse.getUrl();
                System.out.println("URL изображения: " + imageUrl);

                String fileName = Paths.get(new URL(imageUrl).getPath()).getFileName().toString();
                System.out.println("Имя файла: " + fileName);

                downloadImage(httpClient, imageUrl, fileName);

            } finally {
                response.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void downloadImage(CloseableHttpClient httpClient, String imageUrl, String fileName) {
        HttpGet imageRequest = new HttpGet(imageUrl);

        try (CloseableHttpResponse imageResponse = httpClient.execute(imageRequest)) {
            int statusCode = imageResponse.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                System.out.println("Ошибка при скачивании изображения: код ответа " + statusCode);
                return;
            }

            HttpEntity imageEntity = imageResponse.getEntity();
            if (imageEntity != null) {
                java.nio.file.Files.copy(
                        imageEntity.getContent(),
                        new File(fileName).toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
                System.out.println("Изображение успешно сохранено: " + fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class NasaResponse {

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}