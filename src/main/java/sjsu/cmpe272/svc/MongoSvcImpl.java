package sjsu.cmpe272.svc;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;

import sjsu.cmpe272.entity.Forecast;
import sjsu.cmpe272.entity.MonthlyReport;
import sjsu.cmpe272.entity.RegressionForecast;
import sjsu.cmpe272.entity.Reservoir;

@Component
public class MongoSvcImpl implements MongoSvc {
	@Autowired
	MongoOperations mongoOps;

	//@Override
	public void insert(List<Reservoir> documents) {
		mongoOps.insertAll(documents);
	}

	//@Override
	public Reservoir findReservoirByName(String name) {

		Criteria criteria = new Criteria("name");
		criteria.in(name);
		Query query = new Query(criteria);
		Reservoir reservoir = mongoOps.findOne(query, Reservoir.class);
		return reservoir;
	}

	//@Override
	public Reservoir findReservoirDoc(String id) {
		BasicQuery query1 = new BasicQuery("{\"stationId\":\"" + id + "\"}");
		return mongoOps.findOne(query1, Reservoir.class);
	}

	//@Override
	public Forecast findForecastById(String reservoirId) {
		Forecast forecast = new Forecast();

		Reservoir reservoir = findReservoirDoc(reservoirId);
		Map<String, Long> storageData = reservoir.getStorageData();
		Map<String, List<Long>> monthlyBucketMap = new LinkedHashMap<String, List<Long>>();
		// Map<String, Long> monthlyAverageMap = new LinkedHashMap<String,
		// Long>();

		for (Entry<String, Long> entry : storageData.entrySet()) {
			entry.getValue();

			@SuppressWarnings("deprecation")
			Date date = new Date(entry.getKey());
			SimpleDateFormat df2 = new SimpleDateFormat("MM/yyyy");
			String dateText = df2.format(date);

			List<Long> monthlyList = monthlyBucketMap.get(dateText);
			if (monthlyList == null) {
				monthlyList = new ArrayList<Long>();
			}
			monthlyList.add(entry.getValue());
			monthlyBucketMap.put(dateText, monthlyList);

		}

		Map<String, Long> monthlyAverageMap = new LinkedHashMap<String, Long>();
		for (Entry<String, List<Long>> e : monthlyBucketMap.entrySet()) {
			Long avg = sum(e.getValue()) / e.getValue().size();
			monthlyAverageMap.put(e.getKey(), avg);
		}

		Map<String, ArrayList<Long>> forecastData = new LinkedHashMap<String, ArrayList<Long>>();
		Map<String, ArrayList<Long>> forecastDataMonth = new LinkedHashMap<String, ArrayList<Long>>();

		String latestYear = null;

		for (Entry<String, Long> e : monthlyAverageMap.entrySet()) {
			String year = e.getKey().split("/")[1];
			String month = e.getKey().split("/")[0];
			latestYear = year;
			ArrayList<Long> dataPointsForThisYear = forecastData.get(year);
			ArrayList<Long> dataPointsForAllMonths = forecastDataMonth
					.get(month);

			if (dataPointsForThisYear == null) {
				dataPointsForThisYear = new ArrayList<Long>();
			}
			dataPointsForThisYear.add(e.getValue());
			forecastData.put(year, dataPointsForThisYear);

			if (dataPointsForAllMonths == null) {
				dataPointsForAllMonths = new ArrayList<Long>();
			}
			dataPointsForAllMonths.add(e.getValue());
			forecastDataMonth.put(month, dataPointsForAllMonths);

			forecast.setForcastData(forecastData);
			forecast.setForecastDataMonth(forecastDataMonth);

		}

		int nextYear = Integer.parseInt(latestYear) + 1;
		forecastData.put("" + nextYear, average(forecastDataMonth));

		return forecast;
	}

	private Long sum(List<Long> value) {
		long sum = 0;
		for (Long l : value) {
			sum += l;
		}
		return sum;
	}

	private ArrayList<Long> average(
			Map<String, ArrayList<Long>> forecastDataMonth) {
		ArrayList<Long> projectedYearValues = new ArrayList<Long>();
		for (Entry<String, ArrayList<Long>> e : forecastDataMonth.entrySet()) {
			ArrayList<Long> yoyArrayForAMonth = e.getValue();
			Long lastValue = -99L;
			ArrayList<Long> diffArray = new ArrayList<Long>();
			for (Long valueOfAMonth : yoyArrayForAMonth) {
				if (lastValue != -99) {
					Long diff = valueOfAMonth - lastValue;
					diffArray.add(diff);
				}
				lastValue = valueOfAMonth;
			}
			Long projection = (lastValue) - sum(diffArray) / diffArray.size();
			projectedYearValues.add(projection);
		}

		return projectedYearValues;
	}
	
	public List<Reservoir> getReservoirStorageInfo()
	{
		int count = 0;
		List<Reservoir> reservoirInfo = new ArrayList<Reservoir>();
		List<Reservoir> reservoirStorage = new ArrayList<Reservoir>();
		Query query5 = new Query();
		query5.with(new Sort(Sort.Direction.ASC, "_id"));
		reservoirInfo = mongoOps.find(query5, Reservoir.class);
		for(Reservoir r : reservoirInfo)
		{
			if(!r.getStorageData().isEmpty())
			{
				count ++;
				String latE = r.getLatitude();
				String[] parts = latE.split("&");
				String lat = parts[0];
				r.setLatitude(lat);
				
				String longE = r.getLongitude();
				String[] parts1 = longE.split("&");
				String longitude = "-"+parts1[0];
				r.setLongitude(longitude);
				reservoirStorage.add(r);
			}
		}
		System.out.println("Count is  #############"+count);
		return reservoirStorage;
	}
	
	public List<MonthlyReport> getMonthly()
	{
		List<MonthlyReport> monthlyInfo = new ArrayList<MonthlyReport>();
		monthlyInfo = mongoOps.findAll(MonthlyReport.class);
		
		System.out.println("Size of monthly report dats : "+monthlyInfo.size());
		return monthlyInfo;
	}
	
	public List<RegressionForecast> linearRegression()
	{
		SimpleRegression regression = new SimpleRegression(true);
		
		List<RegressionForecast> forecastData = new ArrayList<RegressionForecast>();
		List<MonthlyReport> monthlyInfo = new ArrayList<MonthlyReport>();
		
		Query query5 = new Query();
		query5.with(new Sort(Sort.Direction.ASC, "_id"));
		
		monthlyInfo = mongoOps.find(query5, MonthlyReport.class);
		for(MonthlyReport m : monthlyInfo)
		{
			RegressionForecast forecastMonthly = new RegressionForecast();
			forecastMonthly.set_id(m.get_id());
			forecastMonthly.setAveragecapacity(m.getAveragecapacity());
			forecastData.add(forecastMonthly);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String date = m.get_id();
			Date d = new Date();
			try {
				d = sdf.parse(date);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			double da = d.getTime();
			regression.addData(da, m.getAveragecapacity());
		}
		
		String str = "2014-03-01"; 
		DateFormat dF = new SimpleDateFormat("yyyy-MM-dd"); 
		Date cur_date = new Date();
		try {
			cur_date = dF.parse(str);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(cur_date);
		
		for(int i = 0 ; i < 12 ; i++)
		{
			RegressionForecast forecastMonthly = new RegressionForecast();
			cal.add(Calendar.MONTH, 1);
			Date fore_date = cal.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String stringDate = sdf.format(fore_date);
			
			double next_date = cal.getTimeInMillis();
			System.out.println("next date in milli : "+next_date);
			double forecastVal = regression.predict(next_date);
			System.out.println("forecast val by regression is : "+forecastVal);
			forecastMonthly.set_id(stringDate);
			forecastMonthly.setAveragecapacity(forecastVal);
			forecastData.add(forecastMonthly);
		}
		
		return forecastData;
	}
}
