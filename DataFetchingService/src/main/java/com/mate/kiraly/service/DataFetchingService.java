package com.mate.kiraly.service;

import com.mate.kiraly.dto.SpotyTrackRequest;
import com.mate.kiraly.model.Token;

public interface DataFetchingService {
    Token getToken(String clientId, String clientSecret);
    String getTrackUrl(SpotyTrackRequest spotyTrackRequest);
}
