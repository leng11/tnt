package com.ups.dap.app.tool;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;

import org.openapitools.oauth.client.ApiClient;
import org.openapitools.oauth.client.api.OAuthApi;
import org.openapitools.oauth.client.model.GenerateTokenSuccessResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ups.dap.app.AppConfig;
import com.ups.dap.app.TNTDemo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Util {
	private static String clientCredentials = "client_credentials";
	private static String basicAuth = "Basic ";
	
	public static String getAccessToken(final AppConfig appConfig, final RestTemplate restTemplate) {
		// First try to re-use previously obtained access token.
		// If yes, use it.
		// Otherwise, get an access token and store for future use.
		String accessToken = appConfig.getPreviousObtainedToken();
		if(null == accessToken) {
			accessToken = appConfig.getAccessTokenStore().get(appConfig.getClientID());
		}
		
		if(null == accessToken) {
			OAuthApi oauthApi = new OAuthApi(new ApiClient(restTemplate));
			final String encodedClientIdAndSecret = Base64.getEncoder().encodeToString(
																			(appConfig.getClientID() + ':' + appConfig.getSecret()).
																			getBytes(StandardCharsets.UTF_8));
			oauthApi.getApiClient().setBasePath(appConfig.getOauthBaseUrl());
			oauthApi.getApiClient().addDefaultHeader(HttpHeaders.AUTHORIZATION, basicAuth + encodedClientIdAndSecret);
			log.info("ecnoded clientId and secret: [{}]", encodedClientIdAndSecret);
			
			try {
				GenerateTokenSuccessResponse generateAccessTokenResponse = oauthApi.generateToken(clientCredentials, null);
				accessToken = generateAccessTokenResponse.getAccessToken();		
			} catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
		}
		log.info("access token [{}]", accessToken);
		appConfig.getAccessTokenStore().put(appConfig.getClientID(), accessToken);
		return accessToken;
	}
	
	public static <T> T createRequestFromJsonFile(final String scenarioName, final String filePath,
			final Class<T> requestClass, final AppConfig appConfig, final List<CreateRequestEnricher> enrichers) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			T request = mapper.readValue(TNTDemo.class.getClassLoader().getResourceAsStream(filePath), requestClass);

			enrichers.forEach(enricher -> enricher.enrich(scenarioName, appConfig, request));

			return request;
		} catch (Exception ex) {
			throw new IllegalStateException("failed to constrcut object from [" + filePath + ']', ex);
		}
	}
	
	public static void dayRoll(final Calendar startDay, int days) {
		int offsetBy = 1;
        if(0 > days) {
        	offsetBy = -1;
        	days = Math.abs(days);
        }
        for (int i=0; i<days; i++) {
            do {
            	startDay.add(Calendar.DAY_OF_MONTH, offsetBy);
            } while (!isWeekDay(startDay));
        }
	}
	
	public static boolean isWeekDay(Calendar cal) {
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        return(dayOfWeek != Calendar.SUNDAY && dayOfWeek != Calendar.SATURDAY); 
    } 
	
	private Util() {
	}
}
