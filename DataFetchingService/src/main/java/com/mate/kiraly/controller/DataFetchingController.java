package com.mate.kiraly.controller;

import com.mate.kiraly.dto.SpotyTrackRequest;
import com.mate.kiraly.service.DataFetchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fetch")
public class DataFetchingController {

    private final DataFetchingService dataFetchingService;

    @GetMapping("/track")
    public String getTrackUrl(SpotyTrackRequest spotyTrackRequest){
        return dataFetchingService.getTrackUrl(spotyTrackRequest);
    }
}
