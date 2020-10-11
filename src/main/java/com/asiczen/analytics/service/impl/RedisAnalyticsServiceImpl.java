package com.asiczen.analytics.service.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.asiczen.analytics.repository.RedisConvertedMessageRepository;
import com.asiczen.analytics.response.FleetStatusResponse;
import com.asiczen.analytics.response.FleetUsageResponse;
import com.asiczen.analytics.response.LowFuelAlertResponse;
import com.asiczen.analytics.response.OverSpeedVehiclesResponse;
import com.asiczen.analytics.response.UnderSpeedVehiclesResponse;
import com.asiczen.analytics.response.UnderUtilizedVehicleMatrix;
import com.asiczen.analytics.response.UnderUtilizedVehiclesResponse;
import com.asiczen.analytics.response.VehicleDistanceMatrix;
import com.asiczen.analytics.response.VehicleFuelMatrix;
import com.asiczen.analytics.response.VehicleLastLocResponse;
import com.asiczen.analytics.response.VehicleSpeedMatrix;
import com.asiczen.analytics.service.RedisService;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.MethodDelegationBinder.Record;

@Component
@Slf4j
public class RedisAnalyticsServiceImpl implements RedisService {

	@Autowired
	RedisConvertedMessageRepository redisRepo;

	@Override
	public List<VehicleLastLocResponse> getLastLocationOfVehicles(String orgRefName) {

		return redisRepo.getLastLocationAllVehicles().parallelStream()
				.filter(item -> item.getOrgRefName().equalsIgnoreCase(orgRefName)).map(item -> item)
				.collect(Collectors.toList());
	}

	@Override
	public FleetStatusResponse getFleetStatus(String orgRefName) {

		FleetStatusResponse response = new FleetStatusResponse();

		List<VehicleLastLocResponse> vechiles = redisRepo.getLastLocationAllVehicles().parallelStream()
				.filter(item -> item.getOrgRefName().equalsIgnoreCase(orgRefName)).collect(Collectors.toList());

		// Total Vehicle Count
		response.setTotalVehicle(vechiles.parallelStream().count());

		// No of Vehicles - with no data -- if last message received is 15 minutes older
		response.setNoData(vechiles.parallelStream()
				.filter(record -> (timeDifferenceinMinutes(record.getLastTime()) > 15)).count());

		// Idle -- If distance traveled in last 30 seconds is less than 10 miter
		response.setIdle(vechiles.parallelStream().filter(record -> record.getSpeed() < 20).count());

		// inactive -- When engine is off flag to be checked -- isKey-on-off
		response.setInactive(0L);

		// Running vehicle count -- Speed greater than 20
		response.setRunningVehicle(vechiles.parallelStream().filter(record -> record.getSpeed() >= 20).count());

		// Stopped Vehicles - Engine is on but vehicle speed/distance is nearly 0 for 2
		// minutes
		response.setStopped(0L);

		return response;
	}

	Long timeDifferenceinMinutes(String date) {
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Date time = null;

		long timeinMinutes = 0L;

		try {
			Date currentTime = new Date(System.currentTimeMillis());
			time = dateFormat.parse(date);
			long difference = (currentTime.getTime() - time.getTime());
			timeinMinutes = TimeUnit.MILLISECONDS.toMinutes(difference);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return timeinMinutes;
	}

	@Override
	public FleetUsageResponse getFleetUsage(String orgRefName) {

		FleetUsageResponse response = new FleetUsageResponse();

		List<VehicleLastLocResponse> vehicles = redisRepo.getLastLocationAllVehicles().parallelStream()
				.filter(item -> item.getOrgRefName().equalsIgnoreCase(orgRefName)).collect(Collectors.toList());

		// Total distance traveled by all vehicles at that point in time
		response.setTotalDistance(
				vehicles.parallelStream().mapToDouble(VehicleLastLocResponse::getTotalDistanceDaily).sum());

		// Average distance traveled by vehicles -- Sum of above distance / no of
		// vehicles
		response.setAverage(vehicles.parallelStream().mapToDouble(VehicleLastLocResponse::getTotalDistanceDaily)
				.average().getAsDouble());

		// Prepare vehicle distance matrix
		Set<VehicleDistanceMatrix> data = new HashSet<>();

		vehicles.parallelStream().forEach(record -> data
				.add(new VehicleDistanceMatrix(record.getVehicleNumber(), "NA", record.getTotalDistanceDaily())));

		response.setMatrix(data);

		return response;
	}

	// Vehicles with Fule% less that 10%
	@Override
	public LowFuelAlertResponse getVehiclesWithLowFuel(String orgRefName) {

		LowFuelAlertResponse response = new LowFuelAlertResponse();

		List<VehicleLastLocResponse> vehicles = redisRepo.getLastLocationAllVehicles().parallelStream()
				.filter(item -> item.getOrgRefName().equalsIgnoreCase(orgRefName))
				.filter(record -> record.getFuel() < 11).collect(Collectors.toList());

		// Count vehicles with fuel less than 10%
		response.setCount((int) vehicles.size());

		Set<VehicleFuelMatrix> vehicleDetails = new HashSet<>();
		// Create the set of vehicle
		vehicles.parallelStream().forEach(record -> vehicleDetails
				.add(new VehicleFuelMatrix(record.getVehicleNumber(), record.getDriverName(), record.getFuel())));

		response.setVehicleDetails(vehicleDetails);

		return response;
	}

	// Vehicles with total daily distance traveled is less than 50km
	@Override
	public UnderUtilizedVehiclesResponse getUnderUtilizedVehicles(String orgRefName) {

		UnderUtilizedVehiclesResponse response = new UnderUtilizedVehiclesResponse();

		List<VehicleLastLocResponse> vehicles = redisRepo.getLastLocationAllVehicles().parallelStream()
				.filter(item -> item.getOrgRefName().equalsIgnoreCase(orgRefName))
				.filter(record -> record.getTotalDistanceDaily() < 50d).collect(Collectors.toList());

		// Count of under utilized vehicles.
		response.setCount((int) vehicles.stream().count());

		Set<UnderUtilizedVehicleMatrix> vehicleList = new HashSet<>();
		// now get the list of under utilized vehicles
		vehicles.parallelStream()
				.forEach(record -> vehicleList.add(new UnderUtilizedVehicleMatrix(record.getVehicleNumber(),
						record.getDriverName(), record.getTotalDistanceDaily())));

		response.setVehicleList(vehicleList);
		return response;
	}

	// Vehicles moving at high speed
	@Override
	public OverSpeedVehiclesResponse getOverSpeedingVehicles(String orgRefName) {

		OverSpeedVehiclesResponse response = new OverSpeedVehiclesResponse();

		List<VehicleLastLocResponse> vehicles = redisRepo.getLastLocationAllVehicles().parallelStream()
				.filter(item -> item.getOrgRefName().equalsIgnoreCase(orgRefName))
				.filter(record -> record.getSpeed() > 80d).collect(Collectors.toList());

		response.setCount((int) vehicles.stream().count());

		Set<VehicleSpeedMatrix> vehicleList = new HashSet<>();

		vehicles.parallelStream().forEach(record -> vehicleList
				.add(new VehicleSpeedMatrix(record.getVehicleNumber(), record.getDriverName(), record.getSpeed())));

		response.setVehicleList(vehicleList);
		return response;
	}

	// Vehicles moving at slow speed
	@Override
	public UnderSpeedVehiclesResponse underSpeedVehicles(String orgRefName) {

		UnderSpeedVehiclesResponse response = new UnderSpeedVehiclesResponse();

		List<VehicleLastLocResponse> vehicles = redisRepo.getLastLocationAllVehicles().parallelStream()
				.filter(item -> item.getOrgRefName().equalsIgnoreCase(orgRefName))
				.filter(record -> record.getSpeed() < 20d).filter(record -> record.getSpeed() > 5)
				.collect(Collectors.toList());

		response.setCount((int) vehicles.stream().count());

		Set<VehicleSpeedMatrix> vehicleList = new HashSet<>();

		vehicles.parallelStream().forEach(record -> vehicleList
				.add(new VehicleSpeedMatrix(record.getVehicleNumber(), record.getDriverName(), record.getSpeed())));

		response.setVehicleList(vehicleList);
		return response;

	}

}
