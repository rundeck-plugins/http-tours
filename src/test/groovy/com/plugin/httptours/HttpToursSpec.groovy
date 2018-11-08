package com.plugin.httptours

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import spock.lang.Specification

class HttpToursSpec extends Specification {


    def "Get Tour Manifest"() {
        setup:
        MockWebServer httpServer = new MockWebServer()
        httpServer.start()
        httpServer.enqueue(new MockResponse().setResponseCode(200).setBody('{"name":"Custom Name","tours":[{"key":"tour1","name":"Tour 1"}]}'))
        when:
        HttpTours httpTours = new HttpTours()
        httpTours.tourEndpoint = httpServer.url("")
        httpTours.tourManifestName = "tour-manifest.json"


        then:
        httpTours.tourManifest
        httpServer.takeRequest().path == "/tour-manifest.json"
    }

    def "Get Tour"() {
        setup:
        MockWebServer httpServer = new MockWebServer()
        httpServer.start()
        httpServer.enqueue(new MockResponse().setResponseCode(200).setBody('{"name": "Tour 1","key": "tour1","steps": [{"title": "Tour 1","content": "This is a great tour"}]}'))

        when:
        HttpTours httpTours = new HttpTours()
        httpTours.tourEndpoint = httpServer.url("")
        httpTours.toursSubpath = "tours"
        Map tour = httpTours.getTour("tour1")
        RecordedRequest rrq = httpServer.takeRequest()

        then:
        tour
        rrq.path == "/tours/tour1.json"
        rrq.getHeader("User-Agent") == "httptours/unk (na)"
    }

}