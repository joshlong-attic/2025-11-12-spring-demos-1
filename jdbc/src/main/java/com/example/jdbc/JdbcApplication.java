package com.example.jdbc;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class JdbcApplication {

    public static void main(String[] args) {
        SpringApplication.run(JdbcApplication.class, args);
    }


    @Bean
    ApplicationRunner runner(CustomerService customerService) {
        return args -> {
            var all = customerService.write("A", "B", "C");
            IO.println(all);
        };
    }

}

record Customer(int id, String name) {
}

interface CustomerService {

    Collection<Customer> write(String... names) throws Exception;
}

@Service
class TransactionalCustomerService implements CustomerService {

    private final JdbcClient db;

    TransactionalCustomerService(JdbcClient db) {
        this.db = db;
    }

    private Collection<Customer> read(List<Number> ids) {
        var sql = "select * from customer where id in (" + ids.stream()
                .map(n -> Integer.toString(n.intValue()))
                .collect(Collectors.joining(",")) + ")";
        return db.sql(sql)
                .query((rs, _) -> new Customer(rs.getInt("id"), rs.getString("name")))
                .list();
    }

    @Override
    @Transactional
    public Collection<Customer> write(String... names) throws Exception {
        var keys = new ArrayList<Number>();

        for (var n : names) {
            var kh = new GeneratedKeyHolder();
            this.db
                    .sql("INSERT INTO customer (name) VALUES (?)")
                    .params(n)
                    .update(kh);
            var mapOfWrites = kh.getKeys();
            var id = (Number) mapOfWrites.get("id");
            keys.add(id);

//                if (n.equals("C"))
//                    throw new IllegalStateException("oops!");
        }
        IO.println(keys);
        return this.read(keys);
    }
}

//@Service
class TransactionTemplateCustomerService implements CustomerService {

    private final JdbcClient db;
    private final TransactionTemplate transactionTemplate;

    TransactionTemplateCustomerService(JdbcClient db, TransactionTemplate transactionTemplate) {
        this.db = db;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public Collection<Customer> write(String... names) throws Exception {
        return this.transactionTemplate.execute(status -> {
            for (var n : names) {
                this.db
                        .sql("INSERT INTO customer (name) VALUES (?)")
                        .params(n)
                        .update();
//                if (n.equals("C"))
//                    throw new IllegalStateException("oops!");
            }
            return List.of();
        });
    }
}

//@Service
class JdbcClientCustomerService implements CustomerService {

    private final JdbcClient db;
    private final PlatformTransactionManager transactionManager;

    JdbcClientCustomerService(PlatformTransactionManager transactionManager, JdbcClient db) {
        this.db = db;
        this.transactionManager = transactionManager;
    }

    @Override
    public Collection<Customer> write(String... names) throws Exception {
        var transaction = this.transactionManager
                .getTransaction(TransactionDefinition.withDefaults());
        for (var n : names) {
            this.db
                    .sql("INSERT INTO customer (name) VALUES (?)")
                    .params(n)
                    .update();
            if (n.equals("C"))
                throw new IllegalStateException("oops!");
        }
        this.transactionManager.commit(transaction);
        return new ArrayList<>();
    }
}

// @Service
class PainfulDataSourceCustomerService
        implements CustomerService {


    private final DataSource dataSource;

    PainfulDataSourceCustomerService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Collection<Customer> write(String... names) throws Exception {
        try (var connection = this.dataSource.getConnection()) {
            connection.setAutoCommit(false);
            var customers = new ArrayList<Customer>();
            for (var name : names) {
                if (name.equals("C"))
                    throw new IllegalStateException(
                            "There are " + customers.size() + " customers in the database.");
                this.writeCustomer(connection, name);
                customers.add(new Customer(1, name));
            }

            connection.commit();
            return customers;
        }
    }

    private Customer writeCustomer(Connection connection, String name) throws Exception {
        try (var stmt = connection.prepareStatement(
                "INSERT INTO customer (name) VALUES (?)");) {
            stmt.setString(1, name);
            stmt.execute();
        }
        return null;
    }
}