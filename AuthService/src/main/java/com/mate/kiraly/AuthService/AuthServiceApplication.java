package com.mate.kiraly.AuthService;

import com.mate.kiraly.AuthService.config.RsaKeyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableConfigurationProperties(RsaKeyProperties.class)
@EnableEurekaClient
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}
	//SPOTIFY API RESULT TRACK JSON PATH is: tracks/items[index]/external_urls/spotify
	//SEARCH
	//https://developer.spotify.com/console/get-search-item/?q=&type=&market=&limit=&offset=&include_external=
	//https://developer.spotify.com/documentation/web-api/reference/#/operations/search
	//AUTH
	//https://developer.spotify.com/documentation/general/guides/authorization/client-credentials/
	//EMBED
	//https://developer.spotify.com/documentation/embeds/
}
