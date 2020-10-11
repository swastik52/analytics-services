package com.asiczen.analytics.repository;

import java.util.List;

import com.asiczen.analytics.dto.MsgCount;

public interface AggregateReportCustom {
		
	List<MsgCount> dailyMsgCount();
}
