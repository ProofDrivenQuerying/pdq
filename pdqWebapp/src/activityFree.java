import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class activityFree extends HttpServlet {

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

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		response.setContentType("application/xml");
		PrintWriter out = response.getWriter();
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		try {
			out.println("<activities>");
			out.println("<activityFree>");
			out.println("<activity_comment>" + 1 + "</activity_comment>");
			out.println("<activity_id>" + 2 + "</activity_id>");
			out.println("<assay_chembl_id>" + 3 + "</assay_chembl_id>");
			out.println("<assay_description>" + 4 + "</assay_description>");
			out.println("<assay_type>" + 5 + "</assay_type>");
			out.println("<bao_endpoint>" + 6 + "</bao_endpoint>");
			out.println("<bao_format>" + 7 + "</bao_format>");
			out.println("<canonical_smiles>" + 8 + "</canonical_smiles>");
			out.println("<data_validity_comment>" + 9 + "</data_validity_comment>");
			out.println("<document_chembl_id>" + 10 + "</document_chembl_id>");
			out.println("<document_journal>" + 11 + "</document_journal>");
			out.println("<document_year>" + 12 + "</document_year>");
			out.println("<molecule_chembl_id>" + 13 + "</molecule_chembl_id>");
			out.println("<pchembl_value>" + 14 + "</pchembl_value>");
			out.println("<potential_duplicate>" + 15 + "</potential_duplicate>");
			out.println("<published_relation>" + 16 + "</published_relation>");
			out.println("<published_type>" + 17 + "</published_type>");
			out.println("<published_units>" + 18 + "</published_units>");
			out.println("<published_value>" + 19 + "</published_value>");
			out.println("<qudt_units>" + 20 + "</qudt_units>");
			out.println("<record_id>" + 21 + "</record_id>");
			out.println("<standard_flag>" + 22 + "</standard_flag>");
			out.println("<standard_relation>" + 23 + "</standard_relation>");
			out.println("<standard_type>" + 24 + "</standard_type>");
			out.println("<standard_units>" + 25 + "</standard_units>");
			out.println("<standard_value>" + 26 + "</standard_value>");
			out.println("<target_chembl_id>" + 27 + "</target_chembl_id>");
			out.println("<target_organism>" + 28 + "</target_organism>");
			out.println("<target_pref_name>" + 29 + "</target_pref_name>");
			out.println("<uo_units>" + 30 + "</uo_units>");
			out.println("</activityFree>");
			out.println("</activities>");
		} catch (Exception e) {
			out.println("<pre>" + e.toString() + "</pre>");
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
