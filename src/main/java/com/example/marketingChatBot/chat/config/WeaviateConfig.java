package com.example.marketingChatBot.chat.config;

import io.weaviate.client.WeaviateAuthClient;
import io.weaviate.client.v1.auth.exception.AuthException;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.Config;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import io.weaviate.client.WeaviateClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.weaviate.WeaviateVectorStore;
import org.springframework.ai.vectorstore.weaviate.WeaviateVectorStore.ConsistentLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeaviateConfig {

    @Value("${spring.ai.openai.api-key}")
    private String OPENAI_API_KEY;

    @Value("${spring.ai.vectorstore.weaviate.host}")
    private String WEAVIATE_HOST;

    @Value("${spring.ai.vectorstore.weaviate.api-key}")
    private String WEAVIATE_API_KEY;

    @Bean
    public EmbeddingModel embeddingModel() {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(OPENAI_API_KEY)
                .build();

        return new OpenAiEmbeddingModel(openAiApi);
    }

    @Bean
    public WeaviateClient weaviateClient() throws AuthException {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Openai-Api-Key", OPENAI_API_KEY);
        Config config = new Config("https", WEAVIATE_HOST, headers);
        return WeaviateAuthClient.apiKey(config, WEAVIATE_API_KEY);
    }
    /*@Bean
    public String deleteSchema(WeaviateClient weaviateClient) {
        String className = "BusinessAPI";

        weaviateClient.schema()
                .classDeleter()
                .withClassName(className)
                .run();

        return className;
    }*/

    @Bean
    public WeaviateClass createSchema(WeaviateClient weaviateClient) {
        WeaviateClass businessApiClass = WeaviateClass.builder()
                .className("BusinessAPI")
                .description("API data")
                .vectorizer("text2vec-openai") // OpenAI 임베딩 사용
                .properties(List.of(
                        Property.builder()
                                .name("name")
                                .dataType(List.of("text"))
                                .build(),
                        Property.builder()
                                .name("category")
                                .dataType(List.of("text"))
                                .build(),
                        Property.builder()
                                .name("description")
                                .dataType(List.of("text"))
                                .build(),
                        Property.builder()
                                .name("metadata")
                                .dataType(List.of("text"))
                                .build(),
                        Property.builder()
                                .name("content")
                                .dataType(List.of("text"))
                                .build()
                ))
                .build();

        weaviateClient.schema()
                .classCreator()
                .withClass(businessApiClass)
                .run();

        return businessApiClass;
    }

    @Bean(name = "vectorDB")
    public VectorStore vectorStore(WeaviateClient weaviateClient, EmbeddingModel embeddingModel) {
        return WeaviateVectorStore.builder(weaviateClient, embeddingModel)
                .objectClass("BusinessAPI")
                .consistencyLevel(ConsistentLevel.QUORUM)
                .build();
    }
}
