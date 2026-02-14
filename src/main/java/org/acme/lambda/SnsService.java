package org.acme.lambda;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@ApplicationScoped
public class SnsService {

    @Inject
    SnsClient snsClient;

    public void enviarMensagem(String assunto, String corpo) {
        try {
            snsClient.publish(PublishRequest.builder()
                    .topicArn(System.getenv("TOPICO_ALERTA_ARN"))
                    .subject(assunto)
                    .message(corpo)
                    .build());
            System.out.println("✅ E-mail enviado via SNS: " + assunto);
        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar e-mail SNS: " + e.getMessage());
        }
    }

}
