package com.asiczen.analytics.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import com.asiczen.analytics.dto.Vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(collection = "vehiclemessagesprod")
public class MongoTest extends Vehicle{
	
	private boolean current;
	@DateTimeFormat(iso=ISO.DATE)
	private String dateTimestamp;
	private int unplugged;
	private int fuel;
	private int speed = 0;
	private int distance = 0;
}
