package com.togest.pdfview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class PdfviewApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(PdfviewApplication.class, args);
	}
}
