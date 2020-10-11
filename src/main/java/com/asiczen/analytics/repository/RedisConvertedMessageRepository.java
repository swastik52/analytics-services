package com.asiczen.analytics.repository;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.asiczen.analytics.model.ConvertedMessage;
import com.asiczen.analytics.response.VehicleLastLocResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
public class RedisConvertedMessageRepository {

	@Value("${app.idle.time}")
	private long IDLETIME;

	private static final String KEY = "LASTVINFO";

	private HashOperations<String, String, ConvertedMessage> hashOperations;

	private RedisTemplate<String, ConvertedMessage> redisTemplate;

	public RedisConvertedMessageRepository(RedisTemplate<String, ConvertedMessage> redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.hashOperations = this.redisTemplate.opsForHash();
	}

	public ConvertedMessage get(String vehicleNumber) {

		log.trace("Looking for vehicle Number ", vehicleNumber);
		ObjectMapper objMapper = new ObjectMapper();
		return objMapper.convertValue(hashOperations.get(KEY, vehicleNumber), ConvertedMessage.class);
	}

	public List<VehicleLastLocResponse> getLastLocationAllVehicles() {
		log.trace("Getting vehicle info for all vehicles.");
		List<VehicleLastLocResponse> response = new ArrayList<>();
		ObjectMapper objMapper = new ObjectMapper();
		hashOperations.keys(KEY).parallelStream().map(item -> objMapper.convertValue(item, String.class))
				.collect(Collectors.toList()).parallelStream().map(this::get).collect(Collectors.toList())
				.forEach(data -> {
					log.trace("Record received : {} ", data.toString());
					log.trace("Testing :{} ", data.getVehicleNumber());

					VehicleLastLocResponse loc = new VehicleLastLocResponse();
					try {
						BeanUtils.copyProperties(data, loc);
						loc.setImeiNumber(data.getImeiNumber());
						loc.setLastTime(data.getDateTimestamp());

						loc.setTopSpeed(Math.round(data.getTopSpeed()));
						loc.setTotalDistanceDaily(Math.round(data.getTotalDistanceDaily()));

						try {

							DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
							Date time = dateFormat.parse(data.getDateTimestamp());
							Date currentTime = new Date(System.currentTimeMillis());

							// in mili Seconds
							long difference = (currentTime.getTime() - time.getTime());

							if (difference > IDLETIME) {
								loc.setCurrent(false);
							} else {

								loc.setCurrent(true);
							}

						} catch (Exception ep) {
							log.error("Error while converting the date");
							log.error(ep.getLocalizedMessage());
						}

					} catch (Exception ep) {
						log.error("Error occured while getting last location.....");
						log.error("Redis message processing error...............");
					}
					response.add(loc);
				});
		return response;
	}
}
