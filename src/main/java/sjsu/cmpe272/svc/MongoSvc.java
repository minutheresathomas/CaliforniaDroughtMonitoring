package sjsu.cmpe272.svc;

import java.util.List;

import sjsu.cmpe272.entity.Forecast;
import sjsu.cmpe272.entity.MonthlyReport;
import sjsu.cmpe272.entity.RegressionForecast;
import sjsu.cmpe272.entity.Reservoir;

public interface MongoSvc {

	public void insert(List<Reservoir> documents);

	public Reservoir findReservoirByName(String name);

	public Reservoir findReservoirDoc(String id);

	public Forecast findForecastById(String reservoirId);
	
	public List<Reservoir> getReservoirStorageInfo();
	
	public List<MonthlyReport> getMonthly();
	
	public List<RegressionForecast> linearRegression();

}
