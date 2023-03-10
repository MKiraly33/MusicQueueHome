package com.mate.kiraly.service;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.mate.kiraly.dto.SpotyTokenResponse;
import com.mate.kiraly.dto.SpotyTrackRequest;
import com.mate.kiraly.model.Token;
import com.mate.kiraly.model.Track;
import com.mate.kiraly.repository.DataFetchingRepo;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.json.JSONParser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DataFetchingServiceImpl implements DataFetchingService{

    private final DataFetchingRepo dataFetchingRepo;
    private Token spotyToken = new Token(); //TODO
    private final RestTemplate restTemplate;
    @Value("${spoty.clientID}")
    private String spotyClientId;
    @Value("${spoty.clientSecret}")
    private String spotyClientSecret;

    public DataFetchingServiceImpl(DataFetchingRepo dataFetchingRepo, RestTemplate restTemplate){
        this.dataFetchingRepo = dataFetchingRepo;
        this.restTemplate = restTemplate;
    }
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

        Optional<Track> optionalTrack = dataFetchingRepo.findByArtistAndName(spotyTrackRequest.getArtist(),
                spotyTrackRequest.getName());

        if(optionalTrack.isPresent()){
            return optionalTrack.get().getSpotyLink();
        }

        if(spotyToken.getToken() == null || spotyToken.getExpireAt().isBefore(LocalDateTime.now().minusSeconds(10))){
            spotyToken = getToken(spotyClientId, spotyClientSecret);
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
            String href = jsonObject.getJSONObject("tracks").getJSONArray("items").getJSONObject(0)
                    .getJSONObject("external_urls").getString("spotify");
            Track track = new Track();
            track.setSpotyLink(href);
            track.setArtist(spotyTrackRequest.getArtist());
            track.setName(spotyTrackRequest.getName());
            dataFetchingRepo.save(track);
            return href;
        }
        return null;
    }
}
