<?xml version="1.0" encoding="UTF-8"?>
<query name="\queries\2_1.q" description="" type="conjunctive">
<body>
<atom name="YahooPlaceCode">
<constant value="iso"/>
<constant value="FR"/>
<variable name="_21"/>
</atom>
<atom name="YahooWeather">
<variable name="_21"/>
<variable name="city"/>
<variable name="_15"/>
<variable name="region"/>
<variable name="distance_unit"/>
<variable name="pressure_unit"/>
<variable name="speed_unit"/>
<variable name="temp_unit"/>
<variable name="wind_chill"/>
<variable name="wind_direction"/>
<variable name="wind_speed"/>
<variable name="humidity"/>
<variable name="pressure"/>
<variable name="rising"/>
<variable name="visibility"/>
<variable name="sunrise"/>
<variable name="sunset"/>
<variable name="date"/>
<variable name="temperature"/>
<constant value="Sunny"/>
<variable name="code2"/>
</atom>
<atom name="YahooPlaceRelationship">
<constant value="descendants"/>
<variable name="_21"/>
<variable name="_21"/>
<constant value="PointofInterest"/>
<variable name="_18"/>
<variable name="uri"/>
</atom>
<atom name="YahooPlaces">
<variable name="_21"/>
<variable name="_18"/>
<variable name="type"/>
<constant value="PointofInterest"/>
<variable name="_15"/>
<variable name="admin1"/>
<variable name="admin2"/>
<variable name="admin3"/>
<variable name="locality1"/>
<variable name="locality2"/>
<variable name="postal"/>
<variable name="centroid_lat"/>
<variable name="centroid_lng"/>
<variable name="bboxNorth"/>
<variable name="bboxSouth"/>
<variable name="bboxEast"/>
<variable name="bboxWest"/>
<variable name="timezone"/>
</atom>
</body>
<head name="Q">
<variable name="city"/>
<variable name="_15"/>
<variable name="region"/>
<variable name="temperature"/>
</head>
</query>
