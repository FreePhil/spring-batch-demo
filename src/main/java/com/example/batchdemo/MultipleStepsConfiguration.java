package com.example.batchdemo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class MultipleStepsConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public MultipleStepsConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilder) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilder;
    }

    @Bean
    public Job multipleStepsJob(Step step1, Step step2, Step step3) {
        return this.jobBuilderFactory.get("multipleStepsJob")
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .on("FAILED").to(step2)
                .from(step1).on("*").to(step3)
                .end()
                .build();
    }

    @Bean
    public Step step1() {
        return this.stepBuilderFactory.get("step1")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step1");

                    if (true)
                        throw new Exception("Let's see what happens");

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step step2() {
        return this.stepBuilderFactory.get("step2")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step2");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step step3() {
        return this.stepBuilderFactory.get("step3")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.println("step3");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
