package com.infy.openbanklocal.web.rest;

import com.infy.openbanklocal.web.rest.vm.LoggerVM;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for view and managing Log Level at runtime.
 */
@RestController
@RequestMapping("/management")
public class LogsResource  {
private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LogsResource.class);
@org.springframework.beans.factory.annotation.Autowired
org.springframework.boot.info.BuildProperties buildProperties;
@org.springframework.beans.factory.annotation.Autowired
org.springframework.web.client.RestTemplate restTemplate;
@org.springframework.beans.factory.annotation.Autowired
private io.opentracing.Tracer tracer;
@GetMapping("/ping")
public String ping() {
	LOGGER.info("Ping: name={}, version={}", buildProperties.getName(), buildProperties.getVersion());
	tracer.activeSpan().setBaggageItem("transaction", "ping");
	System.out.println("transaction name"+tracer.activeSpan().getBaggageItem("transaction"));
	String response = restTemplate.getForObject("http://localhost:8091/hello/ping", String.class);
	LOGGER.info("Calling: response={}", response);
	return buildProperties.getName() + ":" + buildProperties.getVersion() + ". Calling... " + response;
}// {

    @GetMapping("/logs")
    public List<LoggerVM> getList() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        return context.getLoggerList()
            .stream()
            .map(LoggerVM::new)
            .collect(Collectors.toList());
    }

    @PutMapping("/logs")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeLevel(@RequestBody LoggerVM jsonLogger) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.getLogger(jsonLogger.getName()).setLevel(Level.valueOf(jsonLogger.getLevel()));
    }
}
