package com.asiczen.analytics.controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asiczen.analytics.model.ConvertedMessage;
import com.asiczen.analytics.repository.ReportRepository;
import com.asiczen.analytics.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/report")
@Slf4j
public class ReportController {
	
	
	@Autowired 
	private ReportRepository repository;
	
	@GetMapping("/msgcount")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<?> countMessagePerDay() {
	
		
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponse(HttpStatus.OK.value(), "Number of Message encountered Today",
						repository.dailyMsgCount()));
		
	}

}
