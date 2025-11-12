package com.example.graphql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestClient;

import java.util.*;

@SpringBootApplication
public class GraphqlApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraphqlApplication.class, args);
    }

}


@Controller
class UsersController {

    private final RestClient http;

    private final Set<User> users = Set.of(
            new User(1, "Jane"),
            new User(2, "John")
    );

    UsersController(RestClient.Builder http) {
        this.http = http.build();
    }

    @QueryMapping
    Collection<User> users() {
        return this.users;
    }

    @BatchMapping
    Map<User, Profile> profile(List<User> userList) {
        IO.println("profiles!!!");
        var map = new HashMap<User, Profile>();
        for (var u : userList) {
            map.put(u, new Profile(1, "linkedin for " + u.id()));
        }
        return map;
    }

//    @SchemaMapping
//    Profile profile(User user) {
//        // todo call profile api for a given user
//        IO.println("profile...");
//        return new Profile(1, "linkedin for " + user.name());
//    }

}

record User(int id, String name) {
}

record Profile(int id, String linkedin) {
}