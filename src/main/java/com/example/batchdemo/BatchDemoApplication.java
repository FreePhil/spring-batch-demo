package com.example.batchdemo;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class BatchDemoApplication {

	static String[] constantArgs = new String[] {
			"batch.input=/Users/phil/input.csv"
	};

	public static void main(String[] args) {
		SpringApplication.run(BatchDemoApplication.class, constantArgs);
	}

}
