package com.asiczen.analytics.repository.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.stereotype.Repository;

import com.asiczen.analytics.controller.AnalyticsController;
import com.asiczen.analytics.dto.MsgCount;
import com.asiczen.analytics.dto.Vehicle;
import com.asiczen.analytics.model.ConvertedMessage;
import com.asiczen.analytics.model.MongoTest;
import com.asiczen.analytics.repository.AggregateReportCustom;
import com.asiczen.analytics.repository.ReportRepository;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class ReportRepositoryImpl implements AggregateReportCustom {

	@Autowired
	MongoTemplate mongoTemplate;
	

	@Override
	public List<MsgCount> dailyMsgCount() {
		LocalDateTime date=LocalDateTime.now().minusDays(1);
		
		log.trace("time"+date);
		Criteria criteria = new Criteria().andOperator(
				Criteria.where("dateTimestamp").gt(date)
			);

		Aggregation agg = Aggregation.newAggregation(Aggregation.match(criteria),
				Aggregation.group("vehicleNumber").count().as("total"),
				Aggregation.project("total").and("vehicleNumber").previousOperation()

		);

		AggregationResults<MsgCount> groupResults = mongoTemplate.aggregate(agg, MongoTest.class, MsgCount.class);
		List<MsgCount> result = groupResults.getMappedResults();

		return result;
	}

}
