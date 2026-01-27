package org.acme.lambda;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import java.util.Map;
import java.util.UUID;

@Path("/avaliacao")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FeedbackLambda {

    @Inject
    DynamoDbClient dynamoDb;

    @POST
    public Response salvarFeedback(Feedback input) {
        try {
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

            return Response.status(Response.Status.CREATED)
                    .entity("{\"mensagem\": \"Feedback salvo com sucesso!\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"erro\": \"" + e.getMessage() + "\"}")
                    .build();
        }
    }
}