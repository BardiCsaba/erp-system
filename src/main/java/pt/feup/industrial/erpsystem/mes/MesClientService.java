package pt.feup.industrial.erpsystem.mes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pt.feup.industrial.erpsystem.dto.MesProductionOrderDto;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Service
public class MesClientService {

    private static final Logger log = LoggerFactory.getLogger(MesClientService.class);

    private final WebClient mesWebClient;

    @Value("${mes.api.productionOrderEndpoint:/api/production-orders}")
    private String productionOrderEndpoint;

    @Autowired
    public MesClientService(WebClient mesWebClient) {
        this.mesWebClient = mesWebClient;
    }

    public boolean sendProductionOrder(MesProductionOrderDto orderRequest) {
        log.debug("Attempting to send production request to MES: {}", orderRequest);
        try {
            mesWebClient.post()
                    .uri(productionOrderEndpoint)
                    .bodyValue(orderRequest)
                    .retrieve()
                    .onStatus(HttpStatus.ACCEPTED::equals, response -> {
                        log.info("MES returned HTTP 202 Accepted for Order Item ID: {}", orderRequest.getErpOrderItemId());
                        return Mono.empty();
                    })
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), clientResponse -> {
                        log.error("MES returned error status {} for Order Item ID: {}", clientResponse.statusCode(), orderRequest.getErpOrderItemId());
                        return Mono.error(new MesCommunicationException("MES returned error: " + clientResponse.statusCode()));
                    })
                    .toBodilessEntity()
                    .block(Duration.ofSeconds(10));

            return true;

        } catch (WebClientRequestException e) {
            log.error("Network error sending request to MES for Order Item ID {}: {}", orderRequest.getErpOrderItemId(), e.getMessage());
            return false;
        } catch (WebClientResponseException e) {
            log.error("HTTP error sending request to MES for Order Item ID {}: Status={}, Body={}",
                    orderRequest.getErpOrderItemId(), e.getRawStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (MesCommunicationException e) {
            log.error("MES communication failed for Order Item ID {}: {}", orderRequest.getErpOrderItemId(), e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error sending request to MES for Order Item ID {}: {}", orderRequest.getErpOrderItemId(), e.getMessage(), e);
            return false;
        }
    }

    private static class MesCommunicationException extends RuntimeException {
        public MesCommunicationException(String message) {
            super(message);
        }
    }
}