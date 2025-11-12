package com.example.graphql_client;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpSyncGraphQlClient;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SpringBootApplication
public class GraphqlClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraphqlClientApplication.class, args);

//        Collection names = new ArrayList();
//        names.add("John");
//        names.add("Jane");
//        names.add(1);
//        Iterator iterator = names.iterator();
//        while (iterator.hasNext()) {
//            Object next =  iterator.next();
//            String name = (String) next;
//            IO.println("the name is " +name);
//        }


        class MyList extends ArrayList<String> {
        }

        Collection<String> names = new ArrayList<String>() {
        };
        names.add("A");
        names.add("B");
//        Collection deref = (Collection) names;
//        deref.add(1);

        Class<? extends Collection> aClass = names.getClass();
        IO.println(aClass.getName());
        IO.println(aClass.getGenericSuperclass());

        IO.println(names.size());
    }


    @Bean
    ApplicationRunner clientDemoApplicationRunner(RestClient.Builder builder) {
        return _ -> {
            var restClient = builder.build();
            var graphQlClient = HttpSyncGraphQlClient
                    .builder(restClient)
                    .url("http://localhost:8080/graphql")
                    .build();


            var results = graphQlClient
                    .document("""
                            query {
                              users {
                                id
                                name
                                profile {
                                  id
                                  linkedin
                                }
                              }
                            }
                            """)
                    .retrieve("users")
//                    .toEntity(JsonNode.class)
                    .toEntity(new ParameterizedTypeReference<List<User>>() {})
                    .block();
            for (var json : results) {
                IO.println(json);
            }
        };
    }
}

record User(int id, String name) {
}

record Profile(int id, String linkedin) {
}