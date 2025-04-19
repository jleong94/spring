package com.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing//Enables batch processing
public class BatchConfig {

	@Autowired
	private JobRepository jobRepository;
	
	@Autowired
	private PlatformTransactionManager platformTransactionManager;

	/**
	 * Defines a sample batch job that executes a single step
	 * 
	 * Flow:
	 * 1. Creates a new job named "sampleJob"
	 * 2. Configures RunIdIncrementer to generate unique run IDs
	 * 3. Sets sampleStep() as the only step in the job
	 * 
	 * @return Job object representing the configured batch job
	 */
	@Bean(name = "sampleJob")
	Job sampleJob() {
		return new JobBuilder("sampleJob", jobRepository)
				.incrementer(new RunIdIncrementer())
		        .start(sampleStep())
		        .build();
	}
	/**
	 * Defines a sample batch step that executes a single tasklet
	 * 
	 * Flow:
	 * 1. Creates a new step named "sampleStep"
	 * 2. Configures the step with a tasklet and transaction manager
	 * 3. Builds and returns the configured step
	 * 
	 * @return Step object representing the configured batch step
	 */
	@Bean(name = "sampleStep") //Defines a step
	Step sampleStep() {
		return new StepBuilder("sampleStep", jobRepository)
				.tasklet(sampleTasklet(), platformTransactionManager) //Assigns tasklet and transaction manager
				.build();
	}
	/**
	 * Defines a sample tasklet that represents the actual work performed in the step
	 * 
	 * Flow:
	 * 1. Takes StepContribution and ChunkContext parameters
	 * 2. Currently just returns FINISHED status without doing any work
	 * 3. Can be enhanced to perform actual batch processing logic
	 * 
	 * @return Tasklet object that executes the step's business logic
	 */
	@Bean
    public Tasklet sampleTasklet() {
        return (contribution, chunkContext) -> {
    		// Add your batch processing logic here
            return RepeatStatus.FINISHED; // Indicates successful completion
        };
    }
}
