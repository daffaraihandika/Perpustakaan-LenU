package com.perpustakaan.perpustakaan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.perpustakaan.perpustakaan.service.PerpustakaanServiceImpl;

import io.grpc.protobuf.services.ProtoReflectionService;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;

@Configuration
public class GrpcConfig {

    @Bean
    public GrpcServerConfigurer serverConfigurer(PerpustakaanServiceImpl perpustakaanService) {
        return serverBuilder -> {
            serverBuilder.addService(ProtoReflectionService.newInstance());
        };
    }
}
