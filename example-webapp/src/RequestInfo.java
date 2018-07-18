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

public class RequestInfo extends HttpServlet {

	/**
	 * @author Mark Ridler
	 * 
	 * This webapp implements a simple retrusn of a table from a Postgres
	 * database. It returns in XML format to be compatible with the REST
	 * services on the client side.
	 * 
	 * This is best deployed as a Dynamic Web Project in Eclipse. This is
	 * not installed by default so you will have to upgrade. Alongside
	 * this goes the Apache Tomcat server which again can be integrated with
	 * Eclipse on the Servers tab. Then rightmouse click on the tomcat and
	 * select "Add and Remove" to add the dynamic web project. After this has
	 * been done you will be able to automatically publish to the tomcat
	 * whenever the source file has changed.
	 * 
	 * The one thing that we haven't been able to get working is the
	 * integration with git. At the moment a sepaarate git project has the
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
        	String query;
        	if((s == null) || (s.equals("")))
        	{
            	query = "SELECT * FROM public.nation;";
        	}
        	else
        	{
        		query = "SELECT * FROM public.nation WHERE n_name = '" + s + "'";
        	}
        	Class.forName("org.postgresql.Driver");
        	Connection con = DriverManager.getConnection("jdbc:postgresql://localhost:5432/tpch?user=postgres&password=root");
        	Statement stmt = con.createStatement();
        	ResultSet rs = stmt.executeQuery(query);
        	out.println("<nations>");
        	while (rs.next()) {
                 int nationkey = rs.getInt("n_nationkey");
                 String name = rs.getString("n_name");
                 int regionkey = rs.getInt("n_regionkey");
                 String comment = rs.getString("n_comment");
                 out.println("<nation><n_nationkey>" + nationkey + "</n_nationkey><n_name>" + name +
                                    "</n_name><n_regionkey>" + regionkey + "</n_regionkey><n_comment>" + comment + "</n_comment></nation>");
            }
            out.println("</nations>");
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
