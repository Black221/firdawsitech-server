package sn.lhacksrt.firdawsitech_server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import sn.lhacksrt.firdawsitech_server.config.FileStorageProperties;

@SpringBootApplication

@EnableConfigurationProperties(FileStorageProperties.class)
public class FirdawsitechServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FirdawsitechServerApplication.class, args);
	}

}
