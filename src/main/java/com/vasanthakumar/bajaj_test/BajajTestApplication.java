package com.vasanthakumar.bajaj_test;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class BajajTestApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(BajajTestApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("name", "VASANTHAKUMAR V");  // Replace with your name
		requestBody.put("regNo", "22BEC7105"); // Replace with your regNo
		requestBody.put("email", "vasanthakumar.22bec7105@vitapstudent.ac.in"); // Replace with your email

		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

		String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
		ResponseEntity<Map> response = restTemplate.exchange(generateUrl, HttpMethod.POST, entity, Map.class);

		Map<String, Object> responseBody = response.getBody();
		if (responseBody == null) {
			System.out.println("Error: No response from generateWebhook");
			return;
		}
		String webhookUrl = (String) responseBody.get("webhook");
		String accessToken = (String) responseBody.get("accessToken");

		if (webhookUrl == null || accessToken == null) {
			System.out.println("Error: Missing webhook or accessToken");
			return;
		}

		System.out.println("Received webhook: " + webhookUrl);
		System.out.println("Received accessToken: " + accessToken);


		String regNo = requestBody.get("regNo");
		String lastTwoDigitsStr = regNo.substring(regNo.length() - 2);
		int lastTwoDigits = Integer.parseInt(lastTwoDigitsStr);
		boolean isOdd = lastTwoDigits % 2 != 0;


		String finalQuery;
		if (isOdd) {

			finalQuery = "SELECT p.AMOUNT AS SALARY, CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, EXTRACT(YEAR FROM AGE(CURRENT_DATE, e.DOB)) AS AGE, d.DEPARTMENT_NAME FROM PAYMENTS p JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID WHERE EXTRACT(DAY FROM p.PAYMENT_TIME) != 1 ORDER BY p.AMOUNT DESC LIMIT 1;";
		} else {
			finalQuery = "SELECT * FROM your_table WHERE condition; -- Replace with actual query";
		}


		Map<String, String> submissionBody = new HashMap<>();
		submissionBody.put("finalQuery", finalQuery);


		// Set headers for submission (including Authorization)
		HttpHeaders submissionHeaders = new HttpHeaders();
		submissionHeaders.setContentType(MediaType.APPLICATION_JSON);
		submissionHeaders.set("Authorization", accessToken);  // Or "Bearer " + accessToken if needed (test it)

		// Create entity for submission
		HttpEntity<Map<String, String>> submissionEntity = new HttpEntity<>(submissionBody, submissionHeaders);

		// Send POST to webhook URL
		ResponseEntity<String> submissionResponse = restTemplate.exchange(webhookUrl, HttpMethod.POST, submissionEntity, String.class);

		// Print response for debugging
		System.out.println("Submission response status: " + submissionResponse.getStatusCode());
		System.out.println("Submission response body: " + submissionResponse.getBody());
	}
}