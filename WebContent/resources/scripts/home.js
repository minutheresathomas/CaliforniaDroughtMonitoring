$(document).ready(

function() {
	
	init();
}

);
var init = function() {
	var json_response = "";
	var json_response_monthly = "";
	var json_response_monthly_forecast = "";
	 var c = 0;
		  alert('inside init function');
	  $.ajax({
			url : "api/home", context : document.body}).done(function(resp) {
				json_response = resp;
//				for(var i in resp) {
//					reservoirs.push([ i, resp[i] ]);
//					c=c+1;
//				}
				google.maps.event.addDomListener(window, 'load', initialize);
				initialize();
		});
	 
function initialize() {
  var mapOptions = {
    zoom: 6,
    center: new google.maps.LatLng(38.000, -120.000)
  };
  
  var map = new google.maps.Map(document.getElementById('map-canvas'),
	      mapOptions);
  
  var i;
  for (i = 0; i < json_response.length; i++) {  
	  
	  var marker = new google.maps.Marker({
	    position: new google.maps.LatLng(json_response[i].latitude, json_response[i].longitude),
	    map: map,
	    title: json_response[i].stationId+'\n'+json_response[i].county+'\n'+'click to zoom'
	  });

	  google.maps.event.addListener(map, 'center_changed', function() {
	    // 3 seconds after the center of the map has changed, pan back to the
	    // marker.
	    window.setTimeout(function() {
	      map.panTo(marker.getPosition());
	    }, 3000);
	  });
	
	  google.maps.event.addListener(marker, 'click', function() {
	    map.setZoom(8);
	    map.setCenter(marker.getPosition());
	  });
  }
}


$.ajax({
		url : "api/home/monthlyReport", context : document.body}).done(function(resp) {
			json_response_monthly = resp;
			drawMonthly();
	});

var drawMonthly = function() {
	alert('monthly');
	var monthlyStorage10 = [];
	var monthlyStorage11 = [];
	var monthlyStorage12 = [];
	var monthlyStorage13 = [];
	var monthlyStorage14 = [];
	
	for ( var i in json_response_monthly)
		{
			if(json_response_monthly[i]._id.indexOf("2010") > -1)
				monthlyStorage10.push([json_response_monthly[i].averagecapacity ]);
			else if(json_response_monthly[i]._id.indexOf("2011") > -1)
				monthlyStorage11.push([json_response_monthly[i].averagecapacity ]);
			else if(json_response_monthly[i]._id.indexOf("2012") > -1)
				monthlyStorage12.push([json_response_monthly[i].averagecapacity ]);
			else if(json_response_monthly[i]._id.indexOf("2013") > -1)
				monthlyStorage13.push([json_response_monthly[i].averagecapacity ]);
			else if(json_response_monthly[i]._id.indexOf("2014") > -1)
				monthlyStorage14.push([json_response_monthly[i].averagecapacity ]);
		}
	
	$('#container').highcharts({
        chart: {
            type: 'areaspline'
        },
        title: {
            text: 'Average Water Capacity in each year'
        },
        legend: {
            layout: 'vertical',
            align: 'left',
            verticalAlign: 'top',
            x: 400,
            y: 200,
            floating: true,
            borderWidth: 1,
            backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColor) || '#FFFFFF'
        },
        xAxis: {
            categories: [
                'Jan',
                'Feb',
                'Mar',
                'Apr',
                'May',
                'Jun',
                'Jul',
                'Aug',
                'Sep',
                'Oct',
                'Nov',
                'Dec'
            ],
            plotBands: [{ // visualize the weekend
                from: 4.5,
                to: 6.5,
                color: 'rgba(68, 170, 213, .2)'
            }]
        },
        yAxis: {
            title: {
                text: 'Capacity units'
            }
        },
        tooltip: {
            shared: true,
            valueSuffix: ' units'
        },
        credits: {
            enabled: false
        },
        plotOptions: {
            areaspline: {
                fillOpacity: 0.5
            }
        },
        series: [{
            name: 'Monthly Storage Capacity - 2010',
            data: monthlyStorage10
        }, {
        	name: 'Monthly Storage Capacity - 2011',
            data: monthlyStorage11
        }, {
        	name: 'Monthly Storage Capacity - 2012',
            data: monthlyStorage12
        }, {
        	name: 'Monthly Storage Capacity - 2013',
            data: monthlyStorage13
        }, {
        	name: 'Monthly Storage Capacity - 2014',
            data: monthlyStorage14
        }]
    });
};

$.ajax({
	url : "api/home/monthlyForecastReport", context : document.body}).done(function(resp) {
		json_response_monthly_forecast = resp;
		drawMonthlyForecast();
});

var drawMonthlyForecast = function() {
	
	var monthlyStorageForecast = [];
	var months = [];
	
	for(var i in json_response_monthly_forecast)
	{
		monthlyStorageForecast.push(json_response_monthly_forecast[i].averagecapacity);
		months.push(json_response_monthly_forecast[i]._id);
	}
	
    $('#container-forecast').highcharts({
    title: {
        text: 'Monthly Forecast',
        x: -20 //center
    },
    subtitle: {
        text: 'California Drought Forecast 2014-2015',
        x: -20
    },
    xAxis: {
        categories: months
    },
    yAxis: {
        title: {
            text: 'Capacity (af)'
        },
        plotLines: [{
            value: 0,
            width: 1,
            color: '#808080'
        }]
    },
    tooltip: {
        valueSuffix: 'in af'
    },
    legend: {
        layout: 'vertical',
        align: 'right',
        verticalAlign: 'middle',
        borderWidth: 0
    },
    series: [{
        name: 'Forecast',
        data: monthlyStorageForecast
    }
    ]
    });
};

};