package com.mate.kiraly.service;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.mate.kiraly.dto.SpotyTokenResponse;
import com.mate.kiraly.dto.SpotyTrackRequest;
import com.mate.kiraly.model.Token;
import com.mate.kiraly.repository.DataFetchingRepo;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DataFetchingServiceImpl implements DataFetchingService{

    private final DataFetchingRepo dataFetchingRepo;
    private Token spotyToken = new Token(); //TODO
    private final RestTemplate restTemplate;
    public Token getToken(String clientId, String clientSecret){
        String url = "https://accounts.spotify.com/api/token";

        //Get auth string
        Base64.Encoder base64Encoder = Base64.getUrlEncoder();
        byte[] toEncode = (clientId + ":" + clientSecret).getBytes();
        String authString = base64Encoder.encodeToString(toEncode);

        //Build headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("Authorization", "Basic " + authString);

        //Build Body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        //Build request
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);

        //Send request
        ResponseEntity<SpotyTokenResponse> response = restTemplate.postForEntity(url, entity, SpotyTokenResponse.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Token token = new Token();
            token.setToken(response.getBody().getAccess_token());
            token.setExpireAt(LocalDateTime.now().plusSeconds(response.getBody().getExpires_in()));
            return token;
        } else {
            return null;
        }
    }

    public String getTrackUrl(SpotyTrackRequest spotyTrackRequest) {
        if(spotyToken.getToken() == null || spotyToken.getExpireAt().isBefore(LocalDateTime.now().minusSeconds(10))){
            spotyToken = getToken("55e38135ccb940e4a0f578ae8bb4a1a1",
                               "<spotyfy_private_key>");
        }
        String url = "https://api.spotify.com/v1/search?type=track&limit=1&market=HU&offset=0&q=artist:{artist}+track:{name}";

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.set("Authorization", "Bearer " + spotyToken.getToken());

        HttpEntity request = new HttpEntity(httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class,
                spotyTrackRequest.getArtist(), spotyTrackRequest.getName());

        if(response.getStatusCode() == HttpStatus.OK) {
            JSONObject jsonObject = new JSONObject(response.getBody());
            return jsonObject.getJSONObject("tracks").getJSONArray("items").getJSONObject(0)
                    .getJSONObject("external_urls").getString("spotify");
        }
        return null;
    }
}
