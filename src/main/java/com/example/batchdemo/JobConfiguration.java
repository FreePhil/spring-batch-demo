package com.example.batchdemo;


import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@Configuration
public class JobConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;
    private Random random;

    public JobConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilder)
    {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilder;
        this.random = new Random();
    }

    @Bean
    public Job job(Step step, Step step2) {
        return this.jobBuilderFactory.get("job")
                .start(step)
                .next(step2)
                .incrementer(new RunIdIncrementer())
                .build();
    }

    @Bean
    public Step step() {
        return this.stepBuilderFactory.get("step")
                .<Integer, Integer>chunk(3)
                .reader(itemReader())
                .writer(itemWriter())
                .listener(new StepExecutionListenerSupport() {
                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        stepExecution.getJobExecution()
                                .getExecutionContext()
                                .putInt("readCount", stepExecution.getReadCount());

                        return super.afterStep(stepExecution);
                    }
                })
                .build();
    }

    @Bean
    public Step step2() {
        return this.stepBuilderFactory.get("step2")
                .tasklet((StepContribution stepContribution, ChunkContext chunkContext) -> {
                    int readCount = chunkContext.getStepContext()
                            .getStepExecution()
                            .getJobExecution()
                            .getExecutionContext()
                            .getInt("readCount");
                    System.out.println("readCount: " + readCount);
                    return RepeatStatus.FINISHED;
                })
//                .tasklet(new Tasklet() {
//                    @Override
//                    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
//                        System.out.println("Hello World");
//                        return RepeatStatus.FINISHED;
//                    }
//                })
                .build();
    }
//
//
//    @Bean
//    public Step step() {
//        return stepBuilderFactory.get("step")
//                .<Integer, Integer>chunk(3)
//                .reader(itemReader())
//                .writer(itemWriter())
//                .build();
//    }
//
    @Bean
    @StepScope
    public ListItemReader<Integer> itemReader() {
        List<Integer> items = new LinkedList<>();
        for (int i = 0; i < random.nextInt(100); i++) {
            items.add(i);
        }
        return new ListItemReader<>(items);
    }

    @Bean
    @StepScope
    public ItemWriter<Integer> itemWriter() {
        return (items) -> {
            for (Integer item : items) {
                int nextInt = random.nextInt(1000);
                Thread.sleep(nextInt);
                if (nextInt % 57 == 0) {
                    throw new Exception("Boom");
                }
                System.out.println("item = " + item);
            }
        };
    }

}
