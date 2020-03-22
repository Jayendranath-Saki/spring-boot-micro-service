package com.micro.service.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.micro.service.domain.Country;
import com.micro.service.domain.CountryResponse;

import io.restassured.RestAssured;

/**
 * @author Sai Kalyan Kumar Kanne
 *
 */
@RestController
public class SimilarCountries {

	private static final Logger LOGGER = LoggerFactory.getLogger(SimilarCountries.class);

	@GetMapping(value = "/country-code/{countryCode}/countryDetails")
	public ResponseEntity<CountryResponse> getCountryByCode(
			@PathVariable(value = "countryCode", required = true) final String countryCode,
			@RequestParam(value = "income-level", required = false) final String incomeLevel1,
			@RequestParam(value = "region", required = false) final String region1,
			@RequestParam(value = "lending-type", required = false) final String lendingType1)
			throws JsonParseException, JsonMappingException, IOException, ParseException {

		final CountryResponse response = new CountryResponse();

		if (null != incomeLevel1 || null != region1 || null != lendingType1) {
			StringBuilder strBuilder = new StringBuilder("http://api.worldbank.org/v2/country?format=json");
			String url = strBuilder.toString();
			String initialResponse = RestAssured.with().relaxedHTTPSValidation().when().get(url).asString();

			System.out.println("Finished calling service" + initialResponse);
			JSONArray countryInfo = (JSONArray) new JSONParser().parse(initialResponse);

			JSONArray countryDetails = (JSONArray) countryInfo.get(1);

			List<Country> countries = new ArrayList<Country>();
			// similar country
			for (int i = 0; i < countryDetails.size(); i++) {
				JSONObject jsonObject = (JSONObject) countryDetails.get(i);
				if (null != incomeLevel1) {
					JSONObject incomeLevelJSONObject = (JSONObject) jsonObject.get("incomeLevel");
					if (null != incomeLevelJSONObject
							&& (null != incomeLevelJSONObject.get("value") || null != incomeLevelJSONObject.get("id"))
							&& ((incomeLevel1.equalsIgnoreCase((String) incomeLevelJSONObject.get("value")))
									|| (incomeLevel1.equalsIgnoreCase((String) incomeLevelJSONObject.get("id"))))) {
						createCountry(countries, jsonObject);
						response.setCountries(countries);
					}
				}
				if (null != region1) {
					JSONObject regionJSONObject = (JSONObject) jsonObject.get("region");
					if (null != regionJSONObject
							&& (null != regionJSONObject.get("value") || null != regionJSONObject.get("id"))
							&& ((region1.equalsIgnoreCase((String) regionJSONObject.get("value")))
									|| (region1.equalsIgnoreCase((String) regionJSONObject.get("id"))))) {
						createCountry(countries, jsonObject);
						response.setCountries(countries);
					}
				}
				if (null != lendingType1) {
					JSONObject lendingTypeJSONObject = (JSONObject) jsonObject.get("lendingType");
					if (null != lendingTypeJSONObject
							&& (null != lendingTypeJSONObject.get("value") || null != lendingTypeJSONObject.get("id"))
							&& ((lendingType1.equalsIgnoreCase((String) lendingTypeJSONObject.get("value")))
									|| (lendingType1.equalsIgnoreCase((String) lendingTypeJSONObject.get("id"))))) {
						createCountry(countries, jsonObject);
						response.setCountries(countries);
					}
				}
			}
			return new ResponseEntity<CountryResponse>(response, HttpStatus.OK);
		} else {
			return new ResponseEntity("BAD_REQUEST", HttpStatus.BAD_REQUEST);
		}

	}

	private void createCountry(List<Country> countries, JSONObject jsonObject) {
		Country country = new Country();
		country.setCountryName((String) jsonObject.get("name"));
		country.setCapitalCity((String) jsonObject.get("capitalCity"));

		countries.add(country);
	}

}
