<?xml version="1.0" encoding="UTF-8"?>
<query name="Sunny points of interests within France" description="" type="conjunctive">
<body>
<atom name="YahooPlaceRelationship">
<constant value="descendants"/>
<variable name="homeCountry"/>
<variable name="myNextVacationSpot"/>
<variable name="x9"/>
<variable name="x10"/>
<variable name="x11"/>
</atom>
<atom name="YahooPlaces">
<variable name="myNextVacationSpot"/>
<variable name="name"/>
<variable name="type"/>
<constant value="Point of Interest"/>
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
<atom name="YahooPlaceCode">
<constant value="iso"/>
<constant value="FR"/>
<variable name="homeCountry"/>
</atom>
<atom name="YahooWeather">
<variable name="myNextVacationSpot"/>
<variable name="city"/>
<variable name="country"/>
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
<variable name="code"/>
</atom>
</body>
<head name="Q">
<variable name="city"/>
<variable name="country"/>
<variable name="region"/>
<variable name="temperature"/>
</head>
</query>