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

		// Step 1: Send POST request to generateWebhook
		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("name", "VASANTHAKUMAR V");
		requestBody.put("regNo", "22BEC7105");
		requestBody.put("email", "vasanthakumar.22bec7105@vitapstudent.ac.in");

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

		// Step 2: Decide which SQL problem to solve (odd/even last 2 digits of regNo)
		String regNo = requestBody.get("regNo");
		String lastTwoDigitsStr = regNo.substring(regNo.length() - 2);
		int lastTwoDigits = Integer.parseInt(lastTwoDigitsStr);
		boolean isOdd = lastTwoDigits % 2 != 0;

		String finalQuery;

		if (isOdd) {
			// Question 1: Highest salary not on 1st day of month
			finalQuery =
					"SELECT p.AMOUNT AS SALARY, " +
							"CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
							"EXTRACT(YEAR FROM AGE(CURRENT_DATE, e.DOB)) AS AGE, " +
							"d.DEPARTMENT_NAME " +
							"FROM PAYMENTS p " +
							"JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
							"JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
							"WHERE EXTRACT(DAY FROM p.PAYMENT_TIME) != 1 " +
							"ORDER BY p.AMOUNT DESC " +
							"LIMIT 1;";
		} else {
			// Question 2: Count employees younger than each employee per department
			finalQuery =
					"SELECT e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, d.DEPARTMENT_NAME, " +
							"COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT " +
							"FROM EMPLOYEE e " +
							"JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
							"LEFT JOIN EMPLOYEE e2 " +
							"ON e.DEPARTMENT = e2.DEPARTMENT " +
							"AND e2.DOB > e.DOB " +
							"GROUP BY e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, d.DEPARTMENT_NAME " +
							"ORDER BY e.EMP_ID DESC;";
		}

		// Step 3: Submit the final query
		Map<String, String> submissionBody = new HashMap<>();
		submissionBody.put("finalQuery", finalQuery);

		HttpHeaders submissionHeaders = new HttpHeaders();
		submissionHeaders.setContentType(MediaType.APPLICATION_JSON);
		submissionHeaders.set("Authorization", accessToken);

		HttpEntity<Map<String, String>> submissionEntity = new HttpEntity<>(submissionBody, submissionHeaders);

		ResponseEntity<String> submissionResponse =
				restTemplate.exchange(webhookUrl, HttpMethod.POST, submissionEntity, String.class);

		System.out.println("Submission response status: " + submissionResponse.getStatusCode());
		System.out.println("Submission response body: " + submissionResponse.getBody());
	}
}
