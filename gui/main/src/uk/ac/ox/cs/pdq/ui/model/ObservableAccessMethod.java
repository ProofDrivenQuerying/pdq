// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.ui.model;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import uk.ac.ox.cs.pdq.datasources.ExecutableAccessMethod;
import uk.ac.ox.cs.pdq.datasources.services.RESTAccessMethod;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;

// TODO: Auto-generated Javadoc
/**
 * The Class ObservableAccessMethod.
 */
public class ObservableAccessMethod {
	
	/** */
	private final SimpleStringProperty name =  new SimpleStringProperty(this, "name");
	
	/**  */
	private final SimpleObjectProperty<String> type =  new SimpleObjectProperty<>(this, "type");
	
	/**  */
	private final SimpleObjectProperty<String> working =  new SimpleObjectProperty<>(this, "working");
	
	/**  */
	private final SimpleListProperty<Integer> inputs =  new SimpleListProperty<>(this, "inputs");
	
	/**
	 * Instantiates a new observable access method.
	 *
	 * @param urlToRead String
	 */
	  public static String getHTML(String urlToRead) throws Exception {
	      StringBuilder result = new StringBuilder();
	      URL url = new URL(urlToRead);
	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	      conn.setRequestMethod("GET");
	      BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	      String line;
	      while ((line = rd.readLine()) != null) {
	         result.append(line);
	      }
	      rd.close();
	      return result.toString();
	   }

	public ObservableAccessMethod( String url, AccessMethodDescriptor b) {
		this.name.set(b.getName());
		this.type.set(getAccessType(b));
		try
		{
			getHTML(url);
			this.working.set("Yes");
		}
		catch(Exception e)
		{
			this.working.set("No");
		}
		this.inputs.set(FXCollections.observableArrayList(b.getInputs()));
	}

	/**
	 * returns the type of AccessMethod
	 * @param b
	 * @return
	 */
	private String getAccessType(AccessMethodDescriptor b){
		if(b.getNumberOfInputs() == 0){
			return "free";
		}else{
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("limited:");
			char sep = '[';
			for(int input: b.getInputs()){
				stringBuffer.append(sep).append(input);
				sep = ',';
			}
			stringBuffer.append(']');
			return stringBuffer.toString();
		}
	}
	
	public ObservableAccessMethod(AccessMethodDescriptor b) {
		this.name.set(b.getName());
		this.type.set(getAccessType(b));
		this.working.set("N/A");
		this.inputs.set(FXCollections.observableArrayList(b.getInputs()));
	}
	/**
	 * 
	 *
	 * TOCOMMENT: What are these properties?
	 */
	public Property<String> nameProperty() {
		return this.name;
	}
	
	/**
	 * 
	 */
	public Property<String> typeProperty() {
		return this.type;
	}
	
	/**
	 
	 */
	public Property<ObservableList<Integer>> inputsProperty() {
		return this.inputs;
	}

	/**
	 * 
	 */
	public String getName() {
		return this.name.get();
	}

	/**
	 
	 */
	public String getType() {
		return this.type.get();
	}
	
	/**
	 
	 */
	public String getWorking() {
		return this.working.get();
	}

	/**
	
	 */
	public List<Integer> getInputs() {
		return this.inputs.get();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.valueOf(this.getName() + ":" + this.getType() + "[" + this.getInputs() + "]");
	}
}
