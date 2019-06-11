import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.*;
import javax.servlet.http.*;

public class activityFree extends HttpServlet {

	/**
	 * @author Mark Ridler
	 * 
	 * This webapp implements a simple return of a table from a Postgres
	 * database. It returns in XML format to be compatible with the REST
	 * services on the client side.
	 * 
	 * This is best deployed as a Dynamic Web Project in Eclipse. This is
	 * not installed by default so you will have to upgrade. Alongside
	 * this goes the Apache Tomcat server which again can be integrated with
	 * Eclipse on the Servers tab. Then right mouse click on the tomcat and
	 * select "Add and Remove" to add the dynamic web project. After this has
	 * been done you will be able to automatically publish to the tomcat
	 * whenever the source file has changed.
	 * 
	 * The one thing that we haven't been able to get working is the
	 * integration with git. At the moment a separate git project has the
	 * source files which must be copied into the dynamic web project and
	 * vice versa
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static Map<String, String> getQueryMap(String query)  
	{  
	    String[] params = query.split("&");  
	    Map<String, String> map = new HashMap<String, String>();  
	    for (String param : params)  
	    {  
	        String name = param.split("=")[0];  
	        String value = param.split("=")[1];  
	        map.put(name, value);  
	    }  
	    return map;  
	}


	public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        response.setContentType("application/xml");
        PrintWriter out = response.getWriter();
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
         try
        {
        	String s = request.getParameter("n_name");
        	String query = "SELECT * FROM public.activity;";
        	Class.forName("org.postgresql.Driver");
        	Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/tpch?user=postgres&password=root");
        	Statement stmt = con.createStatement();
        	ResultSet rs = stmt.executeQuery(query);
            out.println("<activities>");
            while (rs.next()) {
            out.println("<activityFree>");
                 out.println("<activity_comment>" + rs.getString("activity_comment") + "</activity_comment>");
                 out.println("<activity_id>" + rs.getString("activity_id") + "</activity_id>");
                 out.println("<assay_chembl_id>" + rs.getString("assay_chembl_id") + "</assay_chembl_id>");
                 out.println("<assay_description>" + rs.getString("assay_description") + "</assay_description>");
                 out.println("<assay_type>" + rs.getString("assay_type") + "</assay_type>");
                 out.println("<bao_endpoint>" + rs.getString("bao_endpoint") + "</bao_endpoint>");
                 out.println("<bao_format>" + rs.getString("bao_format") + "</bao_format>");
                 out.println("<canonical_smiles>" + rs.getString("canonical_smiles") + "</canonical_smiles>");
                 out.println("<data_validity_comment>" + rs.getString("data_validity_comment") + "</data_validity_comment>");
                 out.println("<document_chembl_id>" + rs.getString("document_chembl_id") + "</document_chembl_id>");
                 out.println("<document_journal>" + rs.getString("document_journal") + "</document_journal>");
                 out.println("<document_year>" + rs.getString("document_year") + "</document_year>");
                 out.println("<molecule_chembl_id>" + rs.getString("molecule_chembl_id") + "</molecule_chembl_id>");
                 out.println("<pchembl_value>" + rs.getString("pchembl_value") + "</pchembl_value>");
                 out.println("<potential_duplicate>" + rs.getString("potential_duplicate") + "</potential_duplicate>");
                 out.println("<published_relation>" + rs.getString("published_relation") + "</published_relation>");
                 out.println("<published_type>" + rs.getString("published_type") + "</published_type>");
                 out.println("<published_units>" + rs.getString("published_units") + "</published_units>");
                 out.println("<published_value>" + rs.getString("published_value") + "</published_value>");
                 out.println("<qudt_units>" + rs.getString("qudt_units") + "</qudt_units>");
                 out.println("<record_id>" + rs.getString("record_id") + "</record_id>");
                 out.println("<standard_flag>" + rs.getString("standard_flag") + "</standard_flag>");
                 out.println("<standard_relation>" + rs.getString("standard_relation") + "</standard_relation>");
                 out.println("<standard_type>" + rs.getString("standard_type") + "</standard_type>");
                 out.println("<standard_units>" + rs.getString("standard_units") + "</standard_units>");
                 out.println("<standard_value>" + rs.getString("standard_value") + "</standard_value>");
                 out.println("<target_chembl_id>" + rs.getString("target_chembl_id") + "</target_chembl_id>");
                 out.println("<target_organism>" + rs.getString("target_organism") + "</target_organism>");
                 out.println("<target_pref_name>" + rs.getString("target_pref_name") + "</target_pref_name>");
                 out.println("<uo_units>" + rs.getString("uo_units") + "</uo_units>");
                 out.println("</activityFree>");
          }
          out.println("</activities>");
        }
        catch(ClassNotFoundException e)
        {
        	out.println(e.toString());       	
        }
        catch(SQLException e)
        {
        	out.println(e.toString());
        }       
    }

    /**
     * We are going to perform the same operations for POST requests
     * as for GET methods, so this method just sends the request to
     * the doGet method.
     */

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        doGet(request, response);
    }
}
