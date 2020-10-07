package com.wiredbraincoffee.productapifunctional;

import com.mongodb.internal.connection.Server;
import com.wiredbraincoffee.productapifunctional.handler.ProductHandler;
import com.wiredbraincoffee.productapifunctional.model.Product;
import com.wiredbraincoffee.productapifunctional.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@SpringBootApplication
public class ProductApiFunctionalApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductApiFunctionalApplication.class, args);
	}

	@Bean
	CommandLineRunner init(ProductRepository repository, ReactiveMongoOperations operations) {
		return args -> {
			Flux<Product> productFlux = Flux.just(
					new Product(null, "Extra Large Latte", 4.99),
					new Product(null, "Extra Large Decaf", 3.99),
					new Product(null, "Large Regular", 2.99)
			)
					.flatMap(repository::save);

			productFlux
					.thenMany(repository.findAll())
					.subscribe(System.out::println);

//			operations.collectionExists(Product.class)
//					.flatMap(exists -> exists ? operations.dropCollection(Product.class) : Mono.just(false))
//					.thenMany(v -> operations.createCollection(Product.class))
//					.thenMany(productFlux)
//					.thenMany(repository.findAll())
//					.subscribe(System.out::println);
		};
	}

	@Bean
	RouterFunction<ServerResponse> routes(ProductHandler handler) {
		return route(GET("/products").and(accept(MediaType.APPLICATION_JSON)), handler::getAllProducts)
				.andRoute(POST("/products").and(accept(MediaType.APPLICATION_JSON)), handler::saveProduct)
				.andRoute(GET("/products/events").and(accept(MediaType.APPLICATION_JSON)), handler::getProductEvents)
				.andRoute(PUT("/products/{id}").and(accept(MediaType.APPLICATION_JSON)), handler::updateProduct)
				.andRoute(GET("/products/{id}").and(accept(MediaType.APPLICATION_JSON)), handler::getProduct);

//		return nest(path("/products"),
//				nest(accept(MediaType.APPLICATION_JSON).or(contentType(MediaType.APPLICATION_JSON).or(accept(MediaType.TEXT_EVENT_STREAM)),
//						route(GET("/"), handler::getAllProducts)
//				.andRoute(method(HttpMe)))))
	}

}
