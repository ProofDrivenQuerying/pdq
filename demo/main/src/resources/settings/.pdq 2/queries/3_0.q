<?xml version="1.0" encoding="UTF-8"?>
<query name="% of people living in low level areas in low income countries." description="" type="conjunctive">
<body>
<atom name="WBData">
<variable name="iso"/>
<variable name="indicator"/>
<constant value="2010"/>
<variable name="countryName"/>
<variable name="value"/>
</atom>
<atom name="WBIndicators">
<variable name="indicator"/>
<variable name="topicId"/>
<constant value="Population living in areas where elevation is below 5 meters (% of total population)"/>
<variable name="sourceId"/>
<variable name="sourceNote"/>
</atom>
<atom name="WBIncomeLevels">
<variable name="incomeLevel"/>
<constant value="Low income"/>
</atom>
<atom name="WBCountries">
<variable name="incomeLevel"/>
<variable name="iso"/>
<variable name="name"/>
<variable name="incomeLevelLabel"/>
<variable name="lendingType"/>
<variable name="capitalCity"/>
<variable name="latitude"/>
<variable name="longitude"/>
</atom>
</body>
<head name="Q">
<variable name="countryName"/>
<variable name="value"/>
</head>
</query>
