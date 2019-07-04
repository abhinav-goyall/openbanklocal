package com.infy.openbanklocal;
import com.infy.openbanklocal.config.ApplicationProperties;
import com.infy.openbanklocal.config.DefaultProfileUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;


import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import io.prometheus.client.CollectorRegistry;


import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;


import java.util.Properties;
import java.security.cert.X509Certificate; 



@Configuration
@ConditionalOnClass(CollectorRegistry.class)
public class BeanConfiguration{
    
    
    @Bean
    public BuildProperties getBuildProps() {
    	return new BuildProperties(new Properties());
    }
     @Bean
    public RestTemplate getRestTemplate() throws Exception {
      TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy)
                  .build();
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        RestTemplate template = new RestTemplate(requestFactory);
        template.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        template.getMessageConverters().add(new StringHttpMessageConverter());
        return template;
    }
    }