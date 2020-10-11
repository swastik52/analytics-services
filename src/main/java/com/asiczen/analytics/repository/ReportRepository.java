package com.asiczen.analytics.repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.asiczen.analytics.dto.Vehicle;
import com.asiczen.analytics.model.ConvertedMessage;

@Repository
public interface ReportRepository extends MongoRepository<ConvertedMessage,String>,AggregateReportCustom{	
	

}
