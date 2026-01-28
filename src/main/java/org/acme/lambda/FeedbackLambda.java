package org.acme.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.Map;
import java.util.UUID;

@Named("feedback")
public class FeedbackLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Inject
    DynamoDbClient dynamoDb;

    @Inject
    ObjectMapper mapper;

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            // 1. Extrair e converter o corpo da String para o objeto Feedback
            Feedback input = mapper.readValue(event.getBody(), Feedback.class);

            // 2. Lógica de persistência (mantém-se igual)
            Map<String, AttributeValue> item = Map.of(
                    "id", AttributeValue.builder().s(UUID.randomUUID().toString()).build(),
                    "descricao", AttributeValue.builder().s(input.getDescricao()).build(),
                    "nota", AttributeValue.builder().n(String.valueOf(input.getNota())).build(),
                    "dataEnvio", AttributeValue.builder().s(java.time.Instant.now().toString()).build()
            );

            dynamoDb.putItem(PutItemRequest.builder()
                    .tableName("Feedback")
                    .item(item)
                    .build());

            // 3. Retornar resposta no formato que o API Gateway exige
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(201)
                    .withBody("{\"mensagem\": \"Feedback salvo com sucesso!\"}");

        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"erro\": \"" + e.getMessage() + "\"}");
        }
    }
}