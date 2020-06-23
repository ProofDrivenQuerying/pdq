// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.ui.io.sql.SQLLikeQueryParser;
import uk.ac.ox.cs.pdq.ui.io.sql.antlr.SQLiteLexer;


public class GUIExampleInput2 {
	
	// test method tests example input
	@Test
	public void test()
	{
		Schema schema = testschema();
	}
	static public Schema testschema() {
		Relation yahooplaces = Relation.create("YahooPlaces",
			new Attribute[] {
					Attribute.create(Integer.class, "woeid"),
					Attribute.create(String.class, "name"),
					Attribute.create(Integer.class, "type"),
					Attribute.create(Integer.class, "placeTypeName"), 
					Attribute.create(Integer.class, "country"),
					Attribute.create(Integer.class, "admin1"),     
					Attribute.create(Integer.class, "admin2"),      
					Attribute.create(Integer.class, "admin3"),      
					Attribute.create(Integer.class, "locality1"),
					Attribute.create(String.class, "locality2"),
					Attribute.create(String.class, "postal"),   
					Attribute.create(Double.class, "centroid_lat"),
					Attribute.create(Double.class, "centroid_lng"),
					Attribute.create(Double.class, "bboxNorth"),
					Attribute.create(Double.class, "bboxSouth"),
					Attribute.create(Double.class, "bboxEast"),  
					Attribute.create(Double.class, "bboxWest"),
					Attribute.create(String.class, "timezone") },
			new AccessMethodDescriptor[] {
					AccessMethodDescriptor.create("yh_geo_name", new Integer[] { 1 }),
					AccessMethodDescriptor.create("yh_geo_woeid", new Integer[] { 0 }),
					AccessMethodDescriptor.create("yh_geo_type", new Integer[] { 2, 3 })});
					

		Relation yahooplacetype = Relation.create("YahooPlaceType",
			new Attribute[] {
					Attribute.create(String.class, "placeTypeName"),
					Attribute.create(Integer.class, "code"),
					Attribute.create(String.class, "uri") },
			new AccessMethodDescriptor[] {
					AccessMethodDescriptor.create("yh_geo_types", new Integer[] { }),
					AccessMethodDescriptor.create("yh_geo_types_name", new Integer[] { 0 })});
		
		Relation yahooplacecommonancestor = Relation.create("YahooPlaceCommonAncestor",
			new Attribute[] {
					Attribute.create(Integer.class, "woeid1"),
					Attribute.create(String.class, "commonAncestor"),
					Attribute.create(Integer.class, "woeid2"),
					Attribute.create(Integer.class, "woeid"),
					Attribute.create(String.class, "placeTypeName"),
					Attribute.create(String.class, "name"),
					Attribute.create(String.class, "uri") },
			new AccessMethodDescriptor[] {
					AccessMethodDescriptor.create("yh_com_anc", new Integer[] { 0, 1 }) } );
		
		Relation yahooplacerelationship = Relation.create("YahooPlaceRelationship",
				new Attribute[] {
						Attribute.create(String.class, "relation"),
						Attribute.create(Integer.class, "of"),
						Attribute.create(Integer.class, "woeid"),
						Attribute.create(String.class, "placeTypeName"),
						Attribute.create(String.class, "name"),
						Attribute.create(String.class, "uri") },
				new AccessMethodDescriptor[] {
						AccessMethodDescriptor.create("yh_geo_rel", new Integer[] { 0 }) } );
			
		Relation yahooplacecode = Relation.create("YahooPlaceCode",
				new Attribute[] {
						Attribute.create(String.class, "namespace"),
						Attribute.create(String.class, "code"),
						Attribute.create(Integer.class, "woeid") },
					new AccessMethodDescriptor[] {
						AccessMethodDescriptor.create("yh_geo_code", new Integer[] { 1 }) } );
			
		Relation yahoocontinents = Relation.create("YahooContinents",
				new Attribute[] {
						Attribute.create(Integer.class, "woeid"),
						Attribute.create(Integer.class, "placeType"),
						Attribute.create(String.class, "placeTypeName"),
						Attribute.create(String.class, "name") },
					new AccessMethodDescriptor[] {
						AccessMethodDescriptor.create("yh_geo_continent", new Integer[] { }) } );
			
		Relation yahoocountries = Relation.create("YahooCountries",
				new Attribute[] {
						Attribute.create(Integer.class, "woeid"),
						Attribute.create(Integer.class, "placeType"),
						Attribute.create(String.class, "placeTypeName"),
						Attribute.create(String.class, "name") },
					new AccessMethodDescriptor[] {
						AccessMethodDescriptor.create("yh_geo_country", new Integer[] { }) } );
			
		Relation yahooseas = Relation.create("YahooSeas",
				new Attribute[] {
						Attribute.create(Integer.class, "woeid"),
						Attribute.create(Integer.class, "placeType"),
						Attribute.create(String.class, "placeTypeName"),
						Attribute.create(String.class, "name") },
					new AccessMethodDescriptor[] {
						AccessMethodDescriptor.create("yh_geo_sea", new Integer[] { }) } );

		Relation yahoooceans = Relation.create("YahooOceans",
				new Attribute[] {
						Attribute.create(Integer.class, "woeid"),
						Attribute.create(Integer.class, "placeType"),
						Attribute.create(String.class, "placeTypeName"),
						Attribute.create(String.class, "name") },
					new AccessMethodDescriptor[] {
						AccessMethodDescriptor.create("yh_geo_ocean", new Integer[] { }) } );
		
		Relation yahooweather = Relation.create("YahooWeather",
				new Attribute[] {
						Attribute.create(Integer.class, "woeid"),
						Attribute.create(String.class, "city"),
						Attribute.create(String.class, "country"),
						Attribute.create(String.class, "region"),
						Attribute.create(String.class, "distance_unit"),
						Attribute.create(String.class, "pressure_unit"),
						Attribute.create(String.class, "speed_unit"),
						Attribute.create(String.class, "temp_unit"),
						Attribute.create(Integer.class, "wind_chill"),
						Attribute.create(Integer.class, "wind_direction"),
						Attribute.create(String.class, "wind_speed"),
						Attribute.create(Double.class, "humidity"),
						Attribute.create(Double.class, "pressure"),
						Attribute.create(Integer.class, "rising"),
						Attribute.create(Double.class, "visibility"),
						Attribute.create(String.class, "sunrise"),
						Attribute.create(String.class, "sunset"),
						Attribute.create(String.class, "date"),
						Attribute.create(Double.class, "temperature"),
						Attribute.create(String.class, "condition"),
						Attribute.create(Integer.class, "code") },
					new AccessMethodDescriptor[] {
						AccessMethodDescriptor.create("yh_wtr_woeid", new Integer[] { 0 }) } );

		Schema schema = new Schema(new Relation[] {			
				yahooplaces,
				yahooplacetype,
				yahooplacecommonancestor,
				yahooplacerelationship,
				yahooplacecode,
				yahoocontinents,
				yahoocountries,
				yahooseas,
				yahoooceans,
				yahooweather});
		return schema;
	}

}
