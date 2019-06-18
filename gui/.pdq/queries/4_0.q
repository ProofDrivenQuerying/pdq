<?xml version="1.0" encoding="UTF-8"?>
<query>
	<body>
		<atom name="Nation">
			<variable name="n_nationkey"  />
			<variable name="n_name"       />
			<variable name="n_regionkey" 	 />
			<variable name="n_comment"  />
		</atom>
		<atom name="Country">
			<variable name="n_nationkey"  />
			<variable name="c_area"       />
		</atom>
	</body>
	<head name="Q">
		<variable name="n_name"	/>
		<variable name="n_regionkey" />
		<variable name="n_comment" />
		<variable name="c_area" />
	</head>
</query>
