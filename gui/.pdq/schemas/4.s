<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<schema>
    <relations>
        <relation name="Nation">
            <attribute name="n_nationkey" type="java.lang.Integer"/>
            <attribute name="n_name" type="java.lang.String"/>
            <attribute name="n_regionkey" type="java.lang.Integer"/>
            <attribute name="n_comment" type="java.lang.String"/>
            <access-method name="pdqWebappNationInput" inputs="0"/>
        </relation>
        <relation name="Country">
            <attribute name="c_nationkey" type="java.lang.Integer"/>
            <attribute name="c_area" type="java.lang.Integer"/>
            <access-method name="pdqWebappCountryFree"/>
        </relation>
    </relations>
     <dependencies>
          <dependency type="TGD">
            <body>
               <atom name="Nation">
                    <variable name="_1"/>
                    <variable name="_4"/>
 	                <variable name="_5"/>
     	            <variable name="_6"/>
                </atom>           	
              </body>
            <head>
               <atom name="Countries">
                    <variable name="_1"/>
                    <variable name="_2"/>
                </atom>
             </head>
        </dependency>
    </dependencies>
</schema>
