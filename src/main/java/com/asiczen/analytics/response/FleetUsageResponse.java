package com.asiczen.analytics.response;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FleetUsageResponse {

	private double totalDistance;
	private double average;
	private Set<VehicleDistanceMatrix> matrix;
}
