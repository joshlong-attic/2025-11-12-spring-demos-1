package com.example.basics;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.List;


// spring (framework) triangle
// 1. portable service abstractions
// 2. dependency injection
// 3. aspect oriented programming

// spring (boot) square
// 4. autoconfiguration


// framework: open for extension, but closed for modification

//@EnableTransactionManagement
@SpringBootApplication
public class BasicsApplication {

    public static void main(String[] args) {
//        var applicationContext = new AnnotationConfigApplicationContext(ApplicationConfiguration.class);
        SpringApplication.run(BasicsApplication.class, args);
    }

    @Bean
    ApplicationRunner customerRunner(CustomerService cs) {
        return args -> {
            for (var c : cs.customers())
                IO.println(c);
        };
    }

}

class MyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        IO.println("postProcessAfterInitialization " + beanName);

//        if (bean.getClass().isAnnotationPresent(Transactional.class)) {
//        }
        if (bean instanceof Tx) {

            // yuck.
            return Proxy.newProxyInstance(getClass().getClassLoader(),
                    bean.getClass().getInterfaces(), new InvocationHandler() {
                        @Override
                        public Object invoke(
                                Object proxy,
                                Method method,
                                Object[] args) throws Throwable {
                            Transactional annotation = method.getAnnotation(Transactional.class);

                            IO.println("before");
                            Object result = method.invoke(bean, args);
                            IO.println("after");
                            return result;
                        }
                    });


        }
        return bean;
    }
}

interface Tx {
}

@Configuration
class MyOtherDataSourceConfiguration {


    @Bean
    static MyBeanPostProcessor myBeanPostProcessor() {
        return new MyBeanPostProcessor();
    }

//    @Bean
//    DataSource dataSource() {
//        IO.println("creating the datasource!");
//        return new EmbeddedDatabaseBuilder()
//                .setType(EmbeddedDatabaseType.H2)
//                .build();
//    }

}

@ComponentScan
@Import(MyOtherDataSourceConfiguration.class)
@Configuration
class ApplicationConfiguration {

//    @Bean
//    DefaultCustomerService customerService(DataSource dataSource) {
//        return new DefaultCustomerService(dataSource);
//    }

}

interface CustomerService {
    Collection<Customer> customers();
}

record Customer(int id, String name) {
}

/*
class TransactionCustomerService implements CustomerService {

    private final CustomerService target;

    TransactionCustomerService(CustomerService target) {
        this.target = target;
    }

    @Override
    public Collection<Customer> customers() {
        try {
            IO.println("creating the transaction!");
            return this.target.customers();
        }//
        finally {
            IO.println("commiting the transaction!");
        }
    }
}*/

@Transactional
@Component
class DefaultCustomerService implements CustomerService {

    private final DataSource dataSource;

    DefaultCustomerService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Collection<Customer> customers() {
        return List.of(new Customer(1, "John Doe"), new Customer(2, "Jane Doe"));
    }

    // RAII - resource acquisition is initialization


}