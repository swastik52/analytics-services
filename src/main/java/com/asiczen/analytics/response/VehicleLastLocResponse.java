package com.asiczen.analytics.response;

import com.asiczen.analytics.dto.Vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class VehicleLastLocResponse extends Vehicle {

	private boolean current;
	private float speed;
	private int fuel;
}
