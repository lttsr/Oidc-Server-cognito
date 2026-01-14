package app.context.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

/**
 * HTTP クライアントクラス
 */
@RequiredArgsConstructor
@Component
public class HttpClient {

    private final RestTemplate restTemplate;

    /**
     * POSTリクエストを送信します。
     *
     * @param url          リクエストURL
     * @param body         リクエストボディ
     * @param responseType レスポンス型
     * @return レスポンス
     */
    public <T> T post(
            String url,
            MultiValueMap<String, String> body,
            Class<T> responseType) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        return restTemplate.postForObject(url, request, responseType);
    }

    /**
     * GETリクエストを送信します。
     *
     * @param url          リクエストURL
     * @param responseType レスポンス型
     * @return レスポンス
     */
    public <T> T get(String url, Class<T> responseType) {
        return restTemplate.getForObject(url, responseType);
    }
}
