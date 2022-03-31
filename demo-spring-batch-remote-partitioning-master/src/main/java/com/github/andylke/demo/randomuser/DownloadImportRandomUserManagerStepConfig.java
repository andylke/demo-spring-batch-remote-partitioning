package com.github.andylke.demo.randomuser;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.integration.partition.RemotePartitioningManagerStepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;

@Configuration
public class DownloadImportRandomUserManagerStepConfig {

  @Autowired private RemotePartitioningManagerStepBuilderFactory stepBuilderFactory;

  @Autowired private DownloadImportRandomUserManagerProperties properties;

  @Bean
  public Step downloadImportRandomUserManagerStep() {
    return stepBuilderFactory
        .get("downloadImportRandomUserManager")
        .partitioner("downloadImportRandomUserWorkerStep", downloadRandomUserPartitioner())
        .gridSize(properties.getGridSize())
        .outputChannel(downloadImportRandomUserRequestsChannel())
        .inputChannel(downloadImportRandomUserRepliesChannel())
        .build();
  }

  @Bean
  public DownloadRandomUserPartitioner downloadRandomUserPartitioner() {
    return new DownloadRandomUserPartitioner(properties.getTotalPage());
  }

  @Bean
  public DirectChannel downloadImportRandomUserRequestsChannel() {
    return new DirectChannel();
  }

  @Bean
  public IntegrationFlow downloadImportRandomUserRequestsFlow(
      ActiveMQConnectionFactory connectionFactory) {
    return IntegrationFlows.from(downloadImportRandomUserRequestsChannel())
        .handle(
            Jms.outboundAdapter(connectionFactory)
                .destination("download-import-random-user-requests"))
        .get();
  }

  @Bean
  public QueueChannel downloadImportRandomUserRepliesChannel() {
    return new QueueChannel();
  }

  @Bean
  public IntegrationFlow downloadImportRandomUserRepliesFlow(
      ActiveMQConnectionFactory connectionFactory) {
    return IntegrationFlows.from(
            Jms.messageDrivenChannelAdapter(connectionFactory)
                .destination("download-import-random-user-replies"))
        .channel(downloadImportRandomUserRepliesChannel())
        .get();
  }
}
