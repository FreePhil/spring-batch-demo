package com.example.batchdemo;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

//@Configuration
public class FileJdbcConfiguration {

    private JobBuilderFactory jobBuilderFactory;
    private StepBuilderFactory stepBuilderFactory;

    public FileJdbcConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Item> itemReader(@Value("#{jobParameters['batch.input']}") String pathToFile) {
        return new FlatFileItemReaderBuilder<Item>()
                .name("itemReader")
                .resource(new PathResource(pathToFile))
                .delimited()
                .names("first", "last", "phone")
                .targetType(Item.class)
                .build();
    }

    @Bean
    public ItemProcessor<Item, Item>  itemProcessor() {
        return new ItemProcessor<Item, Item>() {
            @Override
            public Item process(Item item) throws Exception {
                return new Item(
                        item.getFirst().toLowerCase(),
                        item.getLast().toUpperCase(),
                        item.getPhone());
            }
        };
    }

    @Bean
    public JdbcBatchItemWriter<Item> itemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Item>()
                .dataSource(dataSource)
                .sql("insert into item values (:first, :last, :phone)")
                .beanMapped()
                .build();
    }

    @Bean
    public Job jdbcJob(Step step1, Step step2, Step step3) {
        return this.jobBuilderFactory.get("jdbcJob")
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .build();
    }

    @Bean
    public Step step1(ItemReader<Item> itemReader,
                      ItemProcessor<Item, Item> itemProcessor,
                      ItemWriter<Item> itemWriter) {
        return this.stepBuilderFactory.get("step1")
                .<Item, Item>chunk(10)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(5)
                .build();
    }
}
