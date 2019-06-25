package uk.ac.ox.cs.pdq.rest.jsonobjects;

import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.datasources.tuple.Table;
import uk.ac.ox.cs.pdq.db.tuple.Tuple;
import java.util.List;


public class JsonTable {
    public JsonAttribute[] header;
    public JsonTuple[] data;

    JsonTable(Table t){
        //header
        Attribute[] tableHeader = t.getHeader();

        this.header = new JsonAttribute[tableHeader.length];

        for (int i = 0; i < tableHeader.length; i ++){
            header[i] = new JsonAttribute(tableHeader[i].getName(), tableHeader[i].getType().toString());
        }

        //data
        List<Tuple> tableData = t.getData();
        int n = tableData.size();
        data = new JsonTuple[n];

        for(int j = 0; j < n; j++){
            data[j] = new JsonTuple(tableData.get(j));
        }
    }
}
