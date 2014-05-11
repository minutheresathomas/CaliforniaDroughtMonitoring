$(document).ready(

function() {
	
	init();
}

);
var init = function() {
	var locations = [];
	var reservoirs = [];
	var json_response = "";
	 var c = 0;
		  alert('inside init function');
	  $.ajax({
			url : "api/home", context : document.body}).done(function(resp) {
				json_response = resp;
				for(var i in resp) {
					reservoirs.push([ i, resp[i] ]);
					c=c+1;
//					google.maps.event.addDomListener(window, 'load', initialize);
//					initialize(i);
//					locations.push({stationId:i.stationId, latitude:i.latitude, longitude:i.longitude});
				}
				google.maps.event.addDomListener(window, 'load', initialize);
				initialize();
		});

	  

function initialize() {
//	alert('inside initialize: ==== json response i is : '+json_response[0].latitude);
  var mapOptions = {
    zoom: 6,
    center: new google.maps.LatLng(38.000, -120.000)
  };
  
  var map = new google.maps.Map(document.getElementById('map-canvas'),
	      mapOptions);
  
  var i;
//  alert('b4 loop'+json_response.length);
//  alert("locations lat is : "+ json_response[0].latitude);
//  alert("locations long is : "+ json_response[0].longitude);
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

};