/*
 * Copyright © 2017-2018 Hashmap, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

export default class TbArcgisMap {

    constructor(mapObj, $containerElement, initCallback, defaultZoomLevel, dontFitMapBounds, minZoomLevel) {

        this.defaultZoomLevel = defaultZoomLevel;
        this.dontFitMapBounds = dontFitMapBounds;
        this.minZoomLevel = minZoomLevel;
        this.tooltips = [];
        this.mapObj = mapObj;
        this.map = mapObj.view;
        if (initCallback) {
            setTimeout(initCallback, 0); //eslint-disable-line
        }


    }

    inited() {
        return angular.isDefined(this.map);
    }

    updateMarkerLabel(marker, settings) {
        marker.unbindTooltip();
        marker.bindTooltip('<div style="color: '+ settings.labelColor +';"><b>'+settings.labelText+'</b></div>',
            { className: 'tb-marker-label', permanent: true, direction: 'top', offset: marker.tooltipOffset });
    }

    updateMarkerColor(marker, color) {
        marker.color = color;
    }

    updateMarkerImage(marker, settings, image, maxSize) {
        //TODO
        if(marker){
            if(settings){
                if(image){
                    if(maxSize){
                        return marker;
                    }
                }
            }
        }

    }

    createMarker(location, settings, onClickListener, markerArgs) {
       var tbMap = this;

       var marker = this.mapObj.point(location.latitude, location.longitude, settings.labelText);
       this.map.map.add(marker);
        if(settings.featureLayerURL !=="" && angular.isDefined(settings.featureLayerURL)){
            this.map.map.add(this.mapObj.addFeatureLayer(settings.featureLayerURL));
       }

        if (settings.showLabel) {
            // marker.tooltipOffset = [0, -height + 10];
            // marker.bindTooltip('<div style="color: '+ settings.labelColor +';"><b>'+settings.labelText+'</b></div>',
            //     { className: 'tb-marker-label', permanent: true, direction: 'top', offset: marker.tooltipOffset });
        }

        // if (settings.useMarkerImage) {
        //     this.updateMarkerImage(marker, settings, settings.markerImage, settings.markerImageSize || 34);
        // }

        // if (settings.displayTooltip) {
        //     this.createTooltip(marker, settings.tooltipPattern, settings.tooltipReplaceInfo, markerArgs);
        // }
        function eventHandler(event) {
            // the hitTest() checks to see if any graphics in the view
            // intersect the given screen x, y coordinates
            var evt = null;
            tbMap.mapObj.view.hitTest(event)
            .then(function(resp){
                evt = getGraphics(resp);
            });
            if(evt !== null && angular.isDefined(evt)){
              onClickListener(event);
            }
        }

        function getGraphics(response) {
            // the topmost graphic from the hurricanesLayer
            // and display select attribute values from the
            // graphic to the user
            if (response.results.length) {
                var graphic  = response.results.filter(function(result) {
                return result.graphic === marker.graphics.items[0];
              })[0].graphic;
            }
            return graphic;
        }

        if (onClickListener) {
            this.mapObj.view.on('pointer-up', eventHandler);

              
        }
        
        if(markerArgs){
            markerArgs.test = 1;
        }

        return marker.graphics.items[0].geometry;
    }

    removeMarker(marker) {
        this.map.removeLayer(marker);
    }

    // createTooltip(marker, pattern, replaceInfo, markerArgs) {
    //     var popup = L.popup();
    //     popup.setContent('');
    //     marker.bindPopup(popup, {autoClose: false, closeOnClick: false});
    //     this.tooltips.push( {
    //         markerArgs: markerArgs,
    //         popup: popup,
    //         pattern: pattern,
    //         replaceInfo: replaceInfo
    //     });
    // }

    updatePolylineColor(polyline, settings, color) {
        var style = {
            color: color,
            opacity: settings.strokeOpacity,
            weight: settings.strokeWeight
        };
        polyline.setStyle(style);
    }

    // createPolyline(locations, settings) {
    //     var polyline = L.polyline(locations,
    //         {
    //             color: settings.color,
    //             opacity: settings.strokeOpacity,
    //             weight: settings.strokeWeight
    //         }
    //     ).addTo(this.map);
    //     return polyline;
    // }

    removePolyline(polyline) {
        this.map.removeLayer(polyline);
    }

    fitBounds() {
        if (this.dontFitMapBounds && this.defaultZoomLevel) {
                this.zoom = this.defaultZoomLevel;
        }
        else {
            this.zoom = 10;
        }

        // if (bounds.isValid()) {
        //     if (this.dontFitMapBounds && this.defaultZoomLevel) {
        //         this.map.setZoom(this.defaultZoomLevel, {animate: false});
        //         this.map.panTo(bounds.getCenter(), {animate: false});
        //     } else {
        //         var tbMap = this;
        //         this.map.once('zoomend', function() {
        //             if (!tbMap.defaultZoomLevel && tbMap.map.getZoom() > tbMap.minZoomLevel) {
        //                 tbMap.map.setZoom(tbMap.minZoomLevel, {animate: false});
        //             }
        //         });
        //         this.map.fitBounds(bounds, {padding: [50, 50], animate: false});
        //     }
        // }
    }

      createLatLng(lat, lng, labelText, featureLayerURL) {
        var point = this.mapObj.point(lat, lng, labelText);
        this.map.map.add(point);
        if(featureLayerURL !=="" && angular.isDefined(featureLayerURL)){
            this.map.map.add(this.mapObj.addFeatureLayer(featureLayerURL));
        }
        return point.graphics.items[0].geometry;
      }

    extendBoundsWithMarker(bounds, marker) {
        if(marker){
            return bounds;
        }  
    }

    getMarkerPosition(marker) {
        return marker;
    }

    setMarkerPosition(marker, latLng) {
        marker.latitude = latLng.latitude;
        marker.longitude = latLng.longitude;
    }

    getPolylineLatLngs(polyline) {
        return polyline.getLatLngs();
    }

    setPolylineLatLngs(polyline, latLngs) {
        polyline.setLatLngs(latLngs);
    }

    createBounds(mapObj) {
        return mapObj.view.extend;
    }

    extendBounds(bounds, polyline) {
        if (polyline && polyline.getLatLngs()) {
            bounds.extend(polyline.getBounds());
        }
    }

    invalidateSize() {
       // this.map.onresize();
    }

    getTooltips() {
        return this.tooltips;
    }

}

