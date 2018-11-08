# Http Tours Rundeck Plugin

This plugin loads tours from an http endpoint. 

The plugin expects the structure of the http source to look like:

##### Tour Manifest
http://sub.domain.ext/tour-manifest.json

##### Tours
http://sub.domain.ext/tours/tour1.json
http://sub.domain.ext/tours/tour2.json
http://sub.domain.ext/tours/tour3.json
 
 etc...
 
#### Plugin Properties

*(Required)* Set the base endpoint:
`framework.plugin.TourLoader.httptours.tourEndpoint=http://sub.domain.ext`

*(Optional)* Set the tour manifest file name (default is `tour-manifest.json`):
`framework.plugin.TourLoader.httptours.tourManifestName=tour-manifest.json`

*(Optional)* Set the sub path in which the tour files exist (default is `tours`):
`framework.plugin.TourLoader.httptours.toursSubpath=tours`
