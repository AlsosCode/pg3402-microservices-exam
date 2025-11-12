package no.kristiania.pg3402.gateway.config;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Load Balancer configuration for Docker Compose DNS-based service discovery.
 * Docker Compose creates DNS records that resolve to all container IPs for a service.
 */
@Configuration
public class LoadBalancerConfiguration {

    /**
     * Custom ServiceInstanceListSupplier that uses Docker DNS to discover catalog-service instances.
     * Docker Compose DNS returns all IPs for scaled services.
     */
    @Bean
    public ServiceInstanceListSupplier serviceInstanceListSupplier() {
        return new ServiceInstanceListSupplier() {
            @Override
            public String getServiceId() {
                return "catalog-service";
            }

            @Override
            public Flux<List<ServiceInstance>> get() {
                return Flux.defer(() -> {
                    try {
                        // Docker Compose DNS resolves service name to all container IPs
                        InetAddress[] addresses = InetAddress.getAllByName("catalog-service");
                        List<ServiceInstance> instances = new ArrayList<>();

                        for (int i = 0; i < addresses.length; i++) {
                            String host = addresses[i].getHostAddress();
                            instances.add(new DefaultServiceInstance(
                                    "catalog-service-" + i,
                                    "catalog-service",
                                    host,
                                    8081,
                                    false
                            ));
                        }

                        return Flux.just(instances);
                    } catch (UnknownHostException e) {
                        return Flux.just(new ArrayList<>());
                    }
                });
            }
        };
    }
}
