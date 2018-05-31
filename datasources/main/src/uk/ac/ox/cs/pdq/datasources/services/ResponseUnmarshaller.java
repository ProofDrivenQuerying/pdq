package uk.ac.ox.cs.pdq.datasources.services;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.ac.ox.cs.pdq.datasources.AccessException;
import uk.ac.ox.cs.pdq.datasources.utility.Table;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.util.Tuple;
import uk.ac.ox.cs.pdq.util.TupleType;
import uk.ac.ox.cs.pdq.util.Typed;
import uk.ac.ox.cs.pdq.util.Utility;

/**
 * @author Mark Ridler
 *
 */
// ResponseUnmarshaller provides 2 methods for processItems and processItem applicable to both Json and Xml
public class ResponseUnmarshaller {
	
	protected Attribute[] attributes;
	protected List<String> resultDelimiter = new LinkedList<String>(); 

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Collection<Tuple> processItems (
			List<String> delimiters, Object response, TupleType type,
			Table inputTable) throws AccessException{
		Collection<Tuple> result = new LinkedList<>();
		if (!delimiters.isEmpty() && (response instanceof Map)) {
			int i = 0;
			Object items = null;
			do {
				items = ((Map) response).get(delimiters.get(i));
				if (items != null && items instanceof Map) {
					return this.processItems(
							delimiters.subList(i + 1, delimiters.size()),
							items, type, 
							inputTable);
				} else if (items != null && items instanceof Collection) {
					for (Map<String, Object> m: (Collection<Map<String, Object>>) items) {
						result.add(this.processItem(m, type, inputTable));
					}
					return result;
				}
				i++;
			} while (items == null && i < delimiters.size());
		} else if (response instanceof Collection) {
			for (Object o: ((Collection) response)) {
				result.addAll(this.processItems(delimiters, o, type, inputTable));
			}
		} else if (response instanceof Map) {
			Tuple t = this.processItem((Map) response, type, inputTable);
			if (t != null) {
				result.add(t);
			}
		} else {
			throw new AccessException("Could not deserialize response to Map/Collections.");
		}
		return result;
	}

	protected Tuple processItem(Map<String, Object> item, TupleType type, Table inputTable) {
		Object[] result = new Object[type.size()];
		int i = 0, j = 0;
		boolean hasValue = false;
		Typed[] inputHeader = inputTable.getHeader();
		Tuple first = inputTable.isEmpty() ? null: inputTable.iterator().next();
		for (Attribute column: this.attributes) {
			if (!Arrays.asList(inputHeader).contains(column) || inputTable.size() > 1) {
				result[i]= Utility.cast(column.getType(), extract(column, item));
				hasValue |= result[i] != null;
			} else if ((j = Arrays.asList(inputHeader).indexOf(column)) >=0 ) {
				result[i]= Utility.cast(column.getType(), first.getValue(j));
				hasValue = true;
			}

			i++;
		}
		return hasValue ? type.createTuple(result): null ;
	}

	// This does the equivalent of an output method, selecting a column in an item by attribute
	protected Object extract(Attribute attribute, Map<String, Object> item)
	{
		return item.get(attribute.getName());
	}

}
