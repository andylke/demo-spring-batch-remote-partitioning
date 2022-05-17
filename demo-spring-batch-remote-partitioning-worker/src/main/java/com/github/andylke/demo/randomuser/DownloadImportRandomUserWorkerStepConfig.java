package com.github.andylke.demo.randomuser;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.integration.partition.RemotePartitioningWorkerStepBuilderFactory;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;

import com.github.andylke.demo.user.User;
import com.github.andylke.demo.user.UserRepository;

@Configuration
@EnableConfigurationProperties({DownloadImportRandomUserProperties.class})
public class DownloadImportRandomUserWorkerStepConfig {

  @Autowired private RemotePartitioningWorkerStepBuilderFactory stepBuilderFactory;

  @Autowired private DownloadImportRandomUserProperties properties;

  @Autowired private UserRepository userRepository;

  @Bean
  public Step downloadImportRandomUserWorkerStep() {
    return stepBuilderFactory
        .get("downloadImportRandomUserWorker")
        .inputChannel(downloadImportRandomUserRequestsChannel())
        .outputChannel(downloadImportRandomUserRepliesChannel())
        .<RandomUser, User>chunk(properties.getChunkSize())
        .reader(randomUserRestServiceReader(null, null))
        .processor(randomUserToUserProcessor())
        .writer(userRepositoryWriter())
        .build();
  }

  @Bean
  @StepScope
  public RandomUserRestServiceReader randomUserRestServiceReader(
      @Value("#{stepExecutionContext['startPagePerPartition']}") Integer startPagePerPartition,
      @Value("#{stepExecutionContext['totalPagePerPartition']}") Integer totalPagePerPartition) {
    return new RandomUserRestServiceReader(
        startPagePerPartition, totalPagePerPartition, properties.getPageSize());
  }

  @Bean
  public RandomUserToUserProcessor randomUserToUserProcessor() {
    return new RandomUserToUserProcessor();
  }

  @Bean
  public RepositoryItemWriter<? super User> userRepositoryWriter() {
    return new RepositoryItemWriterBuilder<User>().repository(userRepository).build();
  }

  @Bean
  public QueueChannel downloadImportRandomUserRequestsChannel() {
    return new QueueChannel();
  }

  @Bean
  public IntegrationFlow downloadImportRandomUserRequestsFlow(
      ActiveMQConnectionFactory connectionFactory) {
    return IntegrationFlows.from(
            Jms.messageDrivenChannelAdapter(connectionFactory)
                .destination("download-import-random-user-requests"))
        .channel(downloadImportRandomUserRequestsChannel())
        .get();
  }

  @Bean
  public QueueChannel downloadImportRandomUserRepliesChannel() {
    return new QueueChannel();
  }

  @Bean
  public IntegrationFlow downloadImportRandomUserRepliesFlow(
      ActiveMQConnectionFactory connectionFactory) {
    return IntegrationFlows.from(downloadImportRandomUserRepliesChannel())
        .handle(
            Jms.outboundAdapter(connectionFactory)
                .destination("download-import-random-user-replies"))
        .get();
  }
}
