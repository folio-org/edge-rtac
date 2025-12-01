package org.folio.edge.rtac;

import lombok.extern.log4j.Log4j2;
import org.folio.spring.cql.JpaCqlConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

import static org.folio.common.utils.tls.FipsChecker.getFipsChecksResultString;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, JpaCqlConfiguration.class})
@EnableFeignClients
@Log4j2
public class EdgeRtacApplication {

  public static void main(String[] args) {
    log.info(getFipsChecksResultString());
    SpringApplication.run(EdgeRtacApplication.class, args);
  }

}
