//define global vars
var showMarkerLayer;

function init(){
  // Call to function init in map.js
  initMap();

  //show marker layer
  showMarkerSymbolizer = OpenLayers.Util.applyDefaults(
  {
    externalGraphic: "../js/openlayers/img/marker-blue.png",
    pointRadius: 20,
    fillOpacity: 1
  },
  OpenLayers.Feature.Vector.style["default"]);
  showMarkerStyleMap = new OpenLayers.StyleMap({
    "default": showMarkerSymbolizer,
    "select": {}
  });
	
	
  var showMarkerLayer = new OpenLayers.Layer.Vector(
    "Show Marker Layer",
    {
      styleMap: showMarkerStyleMap,
      displayInLayerSwitcher: false
    });

  // === INIT CONTROLS ===
  var highlightCtrl = new OpenLayers.Control.SelectFeature(showMarkerLayer, {
    hover: true,
    highlightOnly: true,
    renderIntent: "",
    eventListeners: {
      featurehighlighted: showTooltipOnEvent,
      featureunhighlighted: hideTooltipOnEvent
    }
  });
	
  map.addControl(highlightCtrl);
  highlightCtrl.activate();
	
  initConnectionLinkLayer();
}

//////////////////////
// POPUP MANAGEMENT //
//////////////////////

function showTooltipOnEvent(event)
{
  var feature = event.feature;
  var text =  "<h2>"+feature.attributes.area.name + "</h2>";
  if(feature.attributes.area.streetName != null)
    text += "<p>"+feature.attributes.area.streetName + "</p>";
  if(feature.attributes.area.countryCode != null)
    text +="<p>"+feature.attributes.area.countryCode + "</p>";

  var popup = new OpenLayers.Popup.FramedCloud("featurePopup",
    feature.geometry.getBounds().getCenterLonLat(),
    new OpenLayers.Size(100,100),
    text,
    null, true, onPopupClose);
  feature.popup = popup;
  popup.feature = feature;
  map.addPopup(popup);
}

function hideTooltipOnEvent(event)
{
  var feature = event.feature;
  if (feature.popup) {
    popup.feature = null;
    map.removePopup(feature.popup);
    feature.popup.destroy();
    feature.popup = null;
  }
}

function onPopupClose(event)
{
  // 'this' is the popup.
  selectControl.unselect(this.feature);
}

//////////////////////////////////
// SHOW MARKER LAYER MANAGEMENT //
//////////////////////////////////

function initConnectionLinkLayer(){
  if(	$("connectionLink_idCorrespondance").value != null)
  {
    var url = "../json/JSONConnectionLink?connectionLinkId="+$("connectionLink_idCorrespondance").value;
    var bounds = new OpenLayers.Bounds();
    var markPoints = new Array();

    new Ajax.Request(url, {
      method: 'get',
      onSuccess: function(transport) {
        var stopPlaces = eval(transport.responseText);
        stopPlaces.each(function(area){
          if(area.latitude != null && area.longitude != null)
          {
            var markPoint = new OpenLayers.Geometry.Point(area.longitude, area.latitude);
            var markPointXY = markPoint.transform(wgsProjection,geoportalProjection)

            bounds.extend(markPointXY);
            var mark = new OpenLayers.Feature.Vector(markPointXY, {
              'area':area
            });
            markPoints.push(mark.geometry);
            showMarkerLayer.addFeatures([mark]);
          }
        });

        map.addLayers([showMarkerLayer]);
        if(bounds.left != null) // If line has stop areas
        {
          // Hack : reduce zoom to see marker picture
          var zoom = map.getZoomForExtent(bounds, true) - 1;
          var point = barycentre(markPoints);
          map.setCenter(point, zoom);
        }
        else // If line has no stop areas
        {
          map.setCenter(new OpenLayers.LonLat(177169.0,5441595.0),20);
        }
      }
    });
  }
}

function test()
{

}

window.onload = init;