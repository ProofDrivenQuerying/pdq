import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Currently deployed at:
 * http://pdq-webapp.cs.ox.ac.uk:80/webapp/servlets/servlet/NationInput
 *
 */
public class NationInput extends HttpServlet {

	/**
	 * @author Mark Ridler
	 * 
	 *         This webapp implements a simple return of a table from a Postgres
	 *         database. It returns in XML format to be compatible with the REST
	 *         services on the client side.
	 * 
	 *         This is best deployed as a Dynamic Web Project in Eclipse. This is
	 *         not installed by default so you will have to upgrade. Alongside this
	 *         goes the Apache Tomcat server which again can be integrated with
	 *         Eclipse on the Servers tab. Then right mouse click on the tomcat and
	 *         select "Add and Remove" to add the dynamic web project. After this
	 *         has been done you will be able to automatically publish to the tomcat
	 *         whenever the source file has changed.
	 * 
	 *         The one thing that we haven't been able to get working is the
	 *         integration with git. At the moment a separate git project has the
	 *         source files which must be copied into the dynamic web project and
	 *         vice versa
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static Map<String, String> getQueryMap(String query) {
		String[] params = query.split("&");
		Map<String, String> map = new HashMap<String, String>();
		for (String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			map.put(name, value);
		}
		return map;
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("application/xml");
		PrintWriter out = response.getWriter();
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		try {

			int expectedNationKey = -1;
			String s = request.getParameter("n_nationkey");
			if ((s == null) || (s.equals(""))) {
				expectedNationKey = -1;
			} else {
				expectedNationKey = Integer.parseInt(s);
			}
			out.println("<nations>");
			for (int i = 0; i < 2; i++) {
				if (expectedNationKey != -1 && expectedNationKey != (i + 1))
					continue;
				int nationkey = i + 1;
				String name = (i == 0) ? "USA" : "USSR";
				int regionkey = i;
				String comment = "comment";

				out.println(
						"<nation><n_nationkey>" + nationkey + "</n_nationkey><n_name>" + name + "</n_name><n_regionkey>"
								+ regionkey + "</n_regionkey><n_comment>" + comment + "</n_comment></nation>");
			}
			out.println("</nations>");
		} catch (Exception e) {
			out.println("<pre>");
			out.println(e.toString());
			out.println("</pre>");
		}
	}

	/**
	 * We are going to perform the same operations for POST requests as for GET
	 * methods, so this method just sends the request to the doGet method.
	 */

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		doGet(request, response);
	}
}
