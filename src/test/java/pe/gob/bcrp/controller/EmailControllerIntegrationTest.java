package pe.gob.bcrp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import pe.gob.bcrp.model.dto.RequestSendEmail;
import pe.gob.bcrp.model.dto.ResponseDTO;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@DisplayName("EmailController Integration Tests")
class EmailControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Prueba de integración completa - Envío de email exitoso")
    void testCreateEmailIntegration_Success() throws Exception {
        // Given
        RequestSendEmail request = new RequestSendEmail();
        // Configurar el request con datos válidos
        // request.setEmail("test@example.com");
        // request.setSubject("Test Subject");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RequestSendEmail> entity = new HttpEntity<>(request, headers);

        String url = "http://localhost:" + port + "/correo";

        // When
        ResponseEntity<ResponseDTO> response = restTemplate.postForEntity(
                url, entity, ResponseDTO.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("Se envío el mensaje correctamente");
        assertThat(response.getBody().getData()).isNotNull();
    }

    @Test
    @DisplayName("Prueba de integración - GET endpoint")
    void testIndexIntegration() {
        // Given
        String url = "http://localhost:" + port + "/correo";

        // When
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Hello world!");
    }

    @Test
    @DisplayName("Prueba de integración - CORS habilitado")
    void testCorsEnabled() {
        // Given
        String url = "http://localhost:" + port + "/correo";
        HttpHeaders headers = new HttpHeaders();
        headers.setOrigin("http://localhost:3000");
        headers.add("Access-Control-Request-Method", "POST");
        headers.add("Access-Control-Request-Headers", "content-type");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
                url, org.springframework.http.HttpMethod.OPTIONS, entity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getAccessControlAllowOrigin()).isEqualTo("*");
    }
}