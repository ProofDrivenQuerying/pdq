<?xml version="1.0" encoding="UTF-8"?>
<schema name="All APIs" description="">
<sources>
<source name="google" file="services/google-services.xml" discoverer="uk.ac.ox.cs.pdq.runtime.io.xml.ServiceReader"/>
<source name="yahoo" file="services/yahoo-services.xml" discoverer="uk.ac.ox.cs.pdq.runtime.io.xml.ServiceReader"/>
<source name="geonames" file="services/geonames-services.xml" discoverer="uk.ac.ox.cs.pdq.runtime.io.xml.ServiceReader"/>
<source name="worldbank" file="services/worldbank-services.xml" discoverer="uk.ac.ox.cs.pdq.runtime.io.xml.ServiceReader"/>
</sources>
<relations>
<relation name="GNChildren" source="geonames" size="10000000">
<access-method name="gn_children_id" type="LIMITED" inputs="1" cost="1.0"/>
</relation>
<relation name="GNSiblings" source="geonames" size="10000000">
<access-method name="gn_siblings_id" type="LIMITED" inputs="1" cost="1.0"/>
</relation>
<relation name="GNContains" source="geonames" size="10000000">
<access-method name="gn_contains_id" type="LIMITED" inputs="1" cost="1.0"/>
</relation>
<relation name="GNNeighbours" source="geonames" size="10000000">
<access-method name="gn_neighbours_id" type="LIMITED" inputs="1" cost="1.0"/>
<access-method name="gn_neighbours_cc" type="LIMITED" inputs="7" cost="1.0"/>
</relation>
<relation name="GNHierarchy" source="geonames" size="10000000">
<access-method name="gn_hierarchy_id" type="LIMITED" inputs="1" cost="1.0"/>
</relation>
<relation name="GNCountrySubDivision" source="geonames" size="25000">
<access-method name="gn_csubdiv_coord" type="LIMITED" inputs="7,8" cost="1.0"/>
</relation>
<relation name="GNOcean" source="geonames" size="5">
<access-method name="gn_ocean_coord" type="LIMITED" inputs="2,3" cost="1.0"/>
</relation>
<relation name="GNTimezone" source="geonames" size="24">
<access-method name="gn_tz_coord" type="LIMITED" inputs="10,11" cost="1.0"/>
</relation>
<relation name="GNEarthquakes" source="geonames" size="100000000">
<access-method name="gn_quakes_coord" type="LIMITED" inputs="1,2,3,4" cost="1.0"/>
</relation>
<relation name="GNPlaces" source="geonames" size="10000000">
<access-method name="gn_get_id" type="LIMITED" inputs="1" cost="1.0"/>
<access-method name="gn_get_name" type="LIMITED" inputs="2" cost="1.0"/>
<access-method name="gn_get_nametype" type="LIMITED" inputs="2,6" cost="1.0"/>
</relation>
<relation name="GNCountryInfo" source="geonames" size="250">
<access-method name="gn_cinfo" type="FREE" cost="1.0"/>
<access-method name="gn_cinfo_cc" type="LIMITED" inputs="2" cost="1.0"/>
</relation>
<relation name="GNCountryCode" source="geonames" size="250">
<access-method name="gn_ccode_coord" type="LIMITED" inputs="5,6" cost="1.0"/>
</relation>
<relation name="GNCities" source="geonames" size="2500000">
<access-method name="gn_cities_coord" type="LIMITED" inputs="1,2,3,4" cost="1.0"/>
</relation>
<relation name="GNWeather" source="geonames" size="100000000">
<access-method name="gn_weather_bbox" type="LIMITED" inputs="1,2,3,4" cost="1.0"/>
<access-method name="gn_weather_coord" type="LIMITED" inputs="6,7" cost="1.0"/>
<access-method name="gn_weather_icao" type="LIMITED" inputs="8" cost="1.0"/>
</relation>
<relation name="GoogleElevation" source="google" size="100000000">
<access-method name="by_location" type="LIMITED" inputs="1,2" cost="1.0"/>
</relation>
<relation name="GoogleGeoCode" source="google" size="100000000">
<access-method name="by_address" type="LIMITED" inputs="1" cost="5.0"/>
<access-method name="by_coord" type="LIMITED" inputs="2,3" cost="1.0"/>
</relation>
<relation name="GoogleTimezone" source="google" size="100000000">
<access-method name="g_timezone" type="LIMITED" inputs="1,2,3" cost="1.0"/>
</relation>
<relation name="FreeBasePeople" source="google" size="17000000">
</relation>
<relation name="FreeBaseCountries" source="google" size="700">
<access-method name="fb_country_free" type="FREE" cost="100.0"/>
</relation>
<relation name="YahooPlaces" source="yahoo" size="10000000">
<access-method name="yh_geo_name" type="LIMITED" inputs="2" cost="100.0"/>
<access-method name="yh_geo_woeid" type="LIMITED" inputs="1" cost="1.0"/>
<access-method name="yh_geo_type" type="LIMITED" inputs="2,3" cost="50.0"/>
</relation>
<relation name="YahooPlaceType" source="yahoo" size="20">
<access-method name="yh_geo_types" type="FREE" cost="50.0"/>
<access-method name="yh_geo_types_name" type="LIMITED" inputs="2" cost="5.0"/>
</relation>
<relation name="YahooPlaceCommonAncestor" source="yahoo" size="10000000">
<access-method name="yh_com_anc" type="LIMITED" inputs="1,2" cost="25.0"/>
</relation>
<relation name="YahooPlaceRelationship" source="yahoo" size="10000000">
<access-method name="yh_geo_rel" type="LIMITED" inputs="1,2" cost="50.0"/>
</relation>
<relation name="YahooPlaceCode" source="yahoo" size="10000000">
<access-method name="yh_geo_code" type="LIMITED" inputs="1,2" cost="1.0"/>
</relation>
<relation name="YahooContinents" source="yahoo" size="7">
<access-method name="yh_geo_continent" type="FREE" cost="1.0"/>
</relation>
<relation name="YahooCountries" source="yahoo" size="250">
<access-method name="yh_geo_country" type="FREE" cost="1.0"/>
</relation>
<relation name="YahooSeas" source="yahoo" size="51">
<access-method name="yh_geo_sea" type="FREE" cost="1.0"/>
</relation>
<relation name="YahooOceans" source="yahoo" size="5">
<access-method name="yh_geo_ocean" type="FREE" cost="1.0"/>
</relation>
<relation name="YahooWeather" source="yahoo" size="100000000">
<access-method name="yh_wtr_woeid" type="LIMITED" inputs="1" cost="10.0"/>
</relation>
<relation name="WBData" source="worldbank" size="4282265600">
<access-method name="wb_i" type="LIMITED" inputs="2" cost="100.0"/>
<access-method name="wb_ci" type="LIMITED" inputs="1,2" cost="10.0"/>
<access-method name="wb_id" type="LIMITED" inputs="2,3" cost="10.0"/>
<access-method name="wb_cid" type="LIMITED" inputs="1,2,3" cost="1.0"/>
</relation>
<relation name="WBIndicators" source="worldbank" size="8804">
<access-method name="i_free" type="FREE" cost="15.0"/>
<access-method name="i_id" type="LIMITED" inputs="1" cost="1.0"/>
<access-method name="i_topicid" type="LIMITED" inputs="2" cost="15.0"/>
</relation>
<relation name="WBCountries" source="worldbank" size="256">
<access-method name="c_free" type="FREE" cost="1.0"/>
<access-method name="c_il" type="LIMITED" inputs="4" cost="1.0"/>
</relation>
<relation name="WBTopics" source="worldbank" size="19">
<access-method name="t_free" type="FREE" cost="2.0"/>
<access-method name="t_id" type="LIMITED" inputs="1" cost="1.0"/>
</relation>
<relation name="WBIncomeLevels" source="worldbank" size="10">
<access-method name="il_free" type="FREE" cost="1.0"/>
<access-method name="il_id" type="LIMITED" inputs="1" cost="1.0"/>
</relation>
<relation name="WBLendingTypes" source="worldbank" size="10">
<access-method name="lt_free" type="FREE" cost="1.0"/>
<access-method name="lt_id" type="LIMITED" inputs="1" cost="1.0"/>
</relation>
</relations>
<dependencies>
<dependency>
<body>
<atom name="GNChildren">
<variable name="source"/>
<variable name="x0"/>
<variable name="x1"/>
<variable name="x2"/>
<variable name="x3"/>
<variable name="x4"/>
<variable name="x5"/>
<variable name="x6"/>
<variable name="x7"/>
<variable name="x8"/>
</atom>
</body>
<head>
<atom name="GNPlaces">
<variable name="source"/>
<variable name="name"/>
<variable name="topoName"/>
<variable name="class"/>
<variable name="className"/>
<variable name="code"/>
<variable name="codeName"/>
<variable name="country"/>
<variable name="countryId"/>
<variable name="countryCode"/>
<variable name="continentCode"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="admin4"/>
<variable name="admin5"/>
<variable name="adminId1"/>
<variable name="adminId2"/>
<variable name="adminId3"/>
<variable name="adminId4"/>
<variable name="adminId5"/>
<variable name="adminCode1"/>
<variable name="adminCode2"/>
<variable name="adminCode3"/>
<variable name="adminCode4"/>
<variable name="adminCode5"/>
<variable name="wikipedia"/>
<variable name="population"/>
<variable name="elevation"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bbNorth"/>
<variable name="bbSouth"/>
<variable name="bbEast"/>
<variable name="bbWest"/>
<variable name="timezone"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="GNSiblings">
<variable name="source"/>
<variable name="x0"/>
<variable name="x1"/>
<variable name="x2"/>
<variable name="x3"/>
<variable name="x4"/>
<variable name="x5"/>
<variable name="x6"/>
<variable name="x7"/>
</atom>
</body>
<head>
<atom name="GNPlaces">
<variable name="source"/>
<variable name="name"/>
<variable name="topoName"/>
<variable name="class"/>
<variable name="className"/>
<variable name="code"/>
<variable name="codeName"/>
<variable name="country"/>
<variable name="countryId"/>
<variable name="countryCode"/>
<variable name="continentCode"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="admin4"/>
<variable name="admin5"/>
<variable name="adminId1"/>
<variable name="adminId2"/>
<variable name="adminId3"/>
<variable name="adminId4"/>
<variable name="adminId5"/>
<variable name="adminCode1"/>
<variable name="adminCode2"/>
<variable name="adminCode3"/>
<variable name="adminCode4"/>
<variable name="adminCode5"/>
<variable name="wikipedia"/>
<variable name="population"/>
<variable name="elevation"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bbNorth"/>
<variable name="bbSouth"/>
<variable name="bbEast"/>
<variable name="bbWest"/>
<variable name="timezone"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="GNNeighbours">
<variable name="source"/>
<variable name="x0"/>
<variable name="x1"/>
<variable name="x2"/>
<variable name="x3"/>
<variable name="x4"/>
<variable name="x5"/>
<variable name="x6"/>
<variable name="x7"/>
</atom>
</body>
<head>
<atom name="GNPlaces">
<variable name="source"/>
<variable name="name"/>
<variable name="topoName"/>
<variable name="class"/>
<variable name="className"/>
<variable name="code"/>
<variable name="codeName"/>
<variable name="country"/>
<variable name="countryId"/>
<variable name="countryCode"/>
<variable name="continentCode"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="admin4"/>
<variable name="admin5"/>
<variable name="adminId1"/>
<variable name="adminId2"/>
<variable name="adminId3"/>
<variable name="adminId4"/>
<variable name="adminId5"/>
<variable name="adminCode1"/>
<variable name="adminCode2"/>
<variable name="adminCode3"/>
<variable name="adminCode4"/>
<variable name="adminCode5"/>
<variable name="wikipedia"/>
<variable name="population"/>
<variable name="elevation"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bbNorth"/>
<variable name="bbSouth"/>
<variable name="bbEast"/>
<variable name="bbWest"/>
<variable name="timezone"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="GNContains">
<variable name="source"/>
<variable name="x0"/>
<variable name="x1"/>
<variable name="x2"/>
<variable name="x3"/>
<variable name="x4"/>
<variable name="x5"/>
<variable name="x6"/>
<variable name="x7"/>
</atom>
</body>
<head>
<atom name="GNPlaces">
<variable name="source"/>
<variable name="name"/>
<variable name="topoName"/>
<variable name="class"/>
<variable name="className"/>
<variable name="code"/>
<variable name="codeName"/>
<variable name="country"/>
<variable name="countryId"/>
<variable name="countryCode"/>
<variable name="continentCode"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="admin4"/>
<variable name="admin5"/>
<variable name="adminId1"/>
<variable name="adminId2"/>
<variable name="adminId3"/>
<variable name="adminId4"/>
<variable name="adminId5"/>
<variable name="adminCode1"/>
<variable name="adminCode2"/>
<variable name="adminCode3"/>
<variable name="adminCode4"/>
<variable name="adminCode5"/>
<variable name="wikipedia"/>
<variable name="population"/>
<variable name="elevation"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bbNorth"/>
<variable name="bbSouth"/>
<variable name="bbEast"/>
<variable name="bbWest"/>
<variable name="timezone"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="GNHierarchy">
<variable name="source"/>
<variable name="x0"/>
<variable name="x1"/>
<variable name="x2"/>
<variable name="x3"/>
<variable name="x4"/>
<variable name="x5"/>
<variable name="x6"/>
<variable name="x7"/>
</atom>
</body>
<head>
<atom name="GNPlaces">
<variable name="source"/>
<variable name="name"/>
<variable name="topoName"/>
<variable name="class"/>
<variable name="className"/>
<variable name="code"/>
<variable name="codeName"/>
<variable name="country"/>
<variable name="countryId"/>
<variable name="countryCode"/>
<variable name="continentCode"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="admin4"/>
<variable name="admin5"/>
<variable name="adminId1"/>
<variable name="adminId2"/>
<variable name="adminId3"/>
<variable name="adminId4"/>
<variable name="adminId5"/>
<variable name="adminCode1"/>
<variable name="adminCode2"/>
<variable name="adminCode3"/>
<variable name="adminCode4"/>
<variable name="adminCode5"/>
<variable name="wikipedia"/>
<variable name="population"/>
<variable name="elevation"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bbNorth"/>
<variable name="bbSouth"/>
<variable name="bbEast"/>
<variable name="bbWest"/>
<variable name="timezone"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="GNCountryInfo">
<variable name="id"/>
<variable name="countryName"/>
<variable name="countryCode"/>
<variable name="continentCode"/>
<variable name="continentName"/>
<variable name="iso3Alpha"/>
<variable name="isoNumeric"/>
<variable name="fipsCode"/>
<variable name="capital"/>
<variable name="population"/>
<variable name="bbNorth"/>
<variable name="bbSouth"/>
<variable name="bbEast"/>
<variable name="bbWest"/>
<variable name="languages"/>
<variable name="currencyCode"/>
<variable name="areaInSqKm"/>
</atom>
</body>
<head>
<atom name="GNPlaces">
<variable name="source"/>
<variable name="name"/>
<variable name="topoName"/>
<constant value="A"/>
<constant value="country, state, region,..."/>
<variable name="code"/>
<variable name="codeName"/>
<variable name="country"/>
<variable name="countryId"/>
<variable name="countryCode"/>
<variable name="continentCode"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="admin4"/>
<variable name="admin5"/>
<variable name="adminId1"/>
<variable name="adminId2"/>
<variable name="adminId3"/>
<variable name="adminId4"/>
<variable name="adminId5"/>
<variable name="adminCode1"/>
<variable name="adminCode2"/>
<variable name="adminCode3"/>
<variable name="adminCode4"/>
<variable name="adminCode5"/>
<variable name="wikipedia"/>
<variable name="population"/>
<variable name="elevation"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bbNorth"/>
<variable name="bbSouth"/>
<variable name="bbEast"/>
<variable name="bbWest"/>
<variable name="timezone"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="GNCountryInfo">
<variable name="geonameId"/>
<variable name="countryName"/>
<variable name="countryCode"/>
<variable name="continentCode"/>
<variable name="continentName"/>
<variable name="iso3Alpha"/>
<variable name="isoNumeric"/>
<variable name="fipsCode"/>
<variable name="capital"/>
<variable name="population"/>
<variable name="bbNorth"/>
<variable name="bbSouth"/>
<variable name="bbEast"/>
<variable name="bbWest"/>
<variable name="languages"/>
<variable name="currencyCode"/>
<variable name="areaInSqKm"/>
</atom>
</body>
<head>
<atom name="YahooCountries">
<variable name="woeid"/>
<variable name="type"/>
<constant value="Country"/>
<variable name="countryName"/>
</atom>
<atom name="YahooContinents">
<variable name="woeid2"/>
<variable name="type2"/>
<constant value="Continent"/>
<variable name="continentName"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaces">
<variable name="woeid"/>
<variable name="name"/>
<variable name="type"/>
<variable name="placeTypeName"/>
<variable name="country"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="locality1"/>
<variable name="locality2"/>
<variable name="postal"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bboxNorth"/>
<variable name="bboxSouth"/>
<variable name="bboxEast"/>
<variable name="bboxWest"/>
<variable name="timezone"/>
</atom>
</body>
<head>
<atom name="GoogleElevation">
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="elevation"/>
<variable name="resolution"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaces">
<variable name="woeid"/>
<variable name="name"/>
<variable name="type"/>
<variable name="placeTypeName"/>
<variable name="country"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="locality1"/>
<variable name="locality2"/>
<variable name="postal"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bboxNorth"/>
<variable name="bboxSouth"/>
<variable name="bboxEast"/>
<variable name="bboxWest"/>
<variable name="timezone"/>
</atom>
</body>
<head>
<atom name="YahooPlaceType">
<variable name="placeTypeName"/>
<variable name="type"/>
<variable name="uri"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaceRelationship">
<constant value="descendants"/>
<variable name="x"/>
<variable name="y"/>
<variable name="x1"/>
<variable name="x2"/>
<variable name="x3"/>
</atom>
<atom name="YahooPlaceRelationship">
<constant value="children"/>
<variable name="y"/>
<variable name="z"/>
<variable name="y1"/>
<variable name="y2"/>
<variable name="y3"/>
</atom>
</body>
<head>
<atom name="YahooPlaceRelationship">
<constant value="descendants"/>
<variable name="x"/>
<variable name="z"/>
<variable name="z1"/>
<variable name="z2"/>
<variable name="z3"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaceRelationship">
<constant value="children"/>
<variable name="x"/>
<variable name="y"/>
<variable name="x1"/>
<variable name="x2"/>
<variable name="x3"/>
</atom>
<atom name="YahooPlaceRelationship">
<constant value="children"/>
<variable name="y"/>
<variable name="z"/>
<variable name="y1"/>
<variable name="y2"/>
<variable name="y3"/>
</atom>
</body>
<head>
<atom name="YahooPlaceRelationship">
<constant value="descendants"/>
<variable name="x"/>
<variable name="z"/>
<variable name="z1"/>
<variable name="z2"/>
<variable name="z3"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaceRelationship">
<constant value="ancestors"/>
<variable name="x"/>
<variable name="y"/>
<variable name="x1"/>
<variable name="x2"/>
<variable name="x3"/>
</atom>
</body>
<head>
<atom name="YahooPlaceRelationship">
<constant value="descendants"/>
<variable name="y"/>
<variable name="x"/>
<variable name="y1"/>
<variable name="y2"/>
<variable name="y3"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaceRelationship">
<constant value="ancestors"/>
<variable name="x"/>
<variable name="y"/>
<variable name="x1"/>
<variable name="x2"/>
<variable name="x3"/>
</atom>
<atom name="YahooPlaceRelationship">
<constant value="ancestors"/>
<variable name="z"/>
<variable name="y"/>
<variable name="y1"/>
<variable name="y2"/>
<variable name="y3"/>
</atom>
</body>
<head>
<atom name="YahooPlaceCommonAncestor">
<variable name="x"/>
<variable name="z"/>
<variable name="y"/>
<variable name="w2"/>
<variable name="w3"/>
<variable name="w4"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaceCode">
<constant value="iso"/>
<variable name="code"/>
<variable name="woeid"/>
</atom>
</body>
<head>
<atom name="YahooPlaces">
<variable name="woeid"/>
<variable name="name"/>
<variable name="type"/>
<constant value="Country"/>
<variable name="country"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="locality1"/>
<variable name="locality2"/>
<variable name="postal"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bboxNorth"/>
<variable name="bboxSouth"/>
<variable name="bboxEast"/>
<variable name="bboxWest"/>
<variable name="timezone"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaces">
<variable name="woeid"/>
<variable name="name"/>
<variable name="type"/>
<constant value="Country"/>
<variable name="countryName"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="locality1"/>
<variable name="locality2"/>
<variable name="postal"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bboxNorth"/>
<variable name="bboxSouth"/>
<variable name="bboxEast"/>
<variable name="bboxWest"/>
<variable name="timezone"/>
</atom>
</body>
<head>
<atom name="YahooCountries">
<variable name="woeid"/>
<variable name="type"/>
<constant value="Country"/>
<variable name="name"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooCountries">
<variable name="woeid"/>
<variable name="type"/>
<constant value="Country"/>
<variable name="name"/>
</atom>
</body>
<head>
<atom name="YahooPlaces">
<variable name="woeid"/>
<variable name="name"/>
<variable name="type"/>
<constant value="Country"/>
<variable name="countryName"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="locality1"/>
<variable name="locality2"/>
<variable name="postal"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bboxNorth"/>
<variable name="bboxSouth"/>
<variable name="bboxEast"/>
<variable name="bboxWest"/>
<variable name="timezone"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaces">
<variable name="woeid"/>
<variable name="name"/>
<variable name="type"/>
<constant value="Continent"/>
<variable name="countryName"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="locality1"/>
<variable name="locality2"/>
<variable name="postal"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bboxNorth"/>
<variable name="bboxSouth"/>
<variable name="bboxEast"/>
<variable name="bboxWest"/>
<variable name="timezone"/>
</atom>
</body>
<head>
<atom name="YahooContinents">
<variable name="woeid"/>
<variable name="type"/>
<constant value="Continent"/>
<variable name="name"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooContinents">
<variable name="woeid"/>
<variable name="type"/>
<constant value="Continent"/>
<variable name="name"/>
</atom>
</body>
<head>
<atom name="YahooPlaces">
<variable name="woeid"/>
<variable name="name"/>
<variable name="type"/>
<constant value="Continent"/>
<variable name="countryName"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="locality1"/>
<variable name="locality2"/>
<variable name="postal"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bboxNorth"/>
<variable name="bboxSouth"/>
<variable name="bboxEast"/>
<variable name="bboxWest"/>
<variable name="timezone"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaces">
<variable name="woeid"/>
<variable name="name"/>
<variable name="type"/>
<constant value="Sea"/>
<variable name="country"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="locality1"/>
<variable name="locality2"/>
<variable name="postal"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bboxNorth"/>
<variable name="bboxSouth"/>
<variable name="bboxEast"/>
<variable name="bboxWest"/>
<variable name="timezone"/>
</atom>
</body>
<head>
<atom name="YahooSeas">
<variable name="woeid"/>
<variable name="type"/>
<variable name="placeTypeName"/>
<variable name="name"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="YahooPlaces">
<variable name="woeid"/>
<variable name="name"/>
<variable name="type"/>
<constant value="Ocean"/>
<variable name="country"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="locality1"/>
<variable name="locality2"/>
<variable name="postal"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bboxNorth"/>
<variable name="bboxSouth"/>
<variable name="bboxEast"/>
<variable name="bboxWest"/>
<variable name="timezone"/>
</atom>
</body>
<head>
<atom name="YahooOceans">
<variable name="woeid"/>
<variable name="type"/>
<variable name="placeTypeName"/>
<variable name="name"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="FreeBaseCountries">
<variable name="fbid"/>
<variable name="countryName"/>
<variable name="notable"/>
<variable name="capitalCity"/>
<variable name="isoCode"/>
</atom>
</body>
<head>
<atom name="WBCountries">
<variable name="wbid"/>
<variable name="isoCode"/>
<variable name="countryName"/>
<variable name="incomeLevel"/>
<variable name="lendingType"/>
<variable name="capitalCity"/>
<variable name="latitude"/>
<variable name="longitude"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="FreeBaseCountries">
<variable name="fbid"/>
<variable name="countryName"/>
<variable name="notable"/>
<variable name="capitalCity"/>
<variable name="isoCode"/>
</atom>
</body>
<head>
<atom name="YahooPlaces">
<variable name="woeid"/>
<variable name="capitalCity"/>
<variable name="type"/>
<constant value="Town"/>
<variable name="country"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="locality1"/>
<variable name="locality2"/>
<variable name="postal"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bboxNorth"/>
<variable name="bboxSouth"/>
<variable name="bboxEast"/>
<variable name="bboxWest"/>
<variable name="timezone"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="WBCountries">
<variable name="id"/>
<variable name="iso"/>
<variable name="countryName"/>
<variable name="incomeLevel"/>
<variable name="lendingType"/>
<variable name="capitalCity"/>
<variable name="latitude"/>
<variable name="longitude"/>
</atom>
</body>
<head>
<atom name="YahooPlaces">
<variable name="woeid"/>
<variable name="countryName"/>
<variable name="type"/>
<constant value="Country"/>
<variable name="country"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="locality1"/>
<variable name="locality2"/>
<variable name="postal"/>
<variable name="latitude"/>
<variable name="longitude"/>
<variable name="bboxNorth"/>
<variable name="bboxSouth"/>
<variable name="bboxEast"/>
<variable name="bboxWest"/>
<variable name="timezone"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="WBData">
<variable name="iso"/>
<variable name="indicator"/>
<variable name="date"/>
<variable name="countryName"/>
<variable name="value"/>
</atom>
</body>
<head>
<atom name="WBCountries">
<variable name="id"/>
<variable name="iso"/>
<variable name="countryName"/>
<variable name="incomeLevel"/>
<variable name="lendingType"/>
<variable name="capitalCity"/>
<variable name="latitude"/>
<variable name="longitude"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="WBData">
<variable name="country"/>
<variable name="indicator"/>
<variable name="date"/>
<variable name="countryName"/>
<variable name="value"/>
</atom>
</body>
<head>
<atom name="WBIndicators">
<variable name="indicator"/>
<variable name="name"/>
<variable name="sourceId"/>
<variable name="sourceNote"/>
<variable name="topicId"/>
</atom>
</head>
</dependency>
<dependency>
<body>
<atom name="WBIndicators">
<variable name="indicator"/>
<variable name="topicId"/>
<variable name="name"/>
<variable name="sourceId"/>
<variable name="sourceNote"/>
</atom>
</body>
<head>
<atom name="WBTopics">
<variable name="topicId"/>
<variable name="topicName"/>
<variable name="topicSourceNote"/>
</atom>
</head>
</dependency>
</dependencies>
</schema>
