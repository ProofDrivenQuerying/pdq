import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class m4 extends HttpServlet {

    /**
     * @author Brandon Moore
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

    private static void printGetString(PrintWriter out, String rs, String string) throws SQLException {
        out.println("<" + string + ">" + rs + "</" + string + ">");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("application/xml");
        PrintWriter out = response.getWriter();
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        try {
            out.println("<m4s>");
            out.println("<part>");
            printGetString(out, "1", "p_partkey");
            printGetString(out, "Brandon#13", "p_brand");
            printGetString(out, "blush thistle blue yellow saddle", "p_name");
            printGetString(out, "1", "p_size");
            printGetString(out, "LARGE BRUSHED BRASS", "p_type");
            printGetString(out, "902.00", "p_retailprice");
            printGetString(out, "lar accounts amo", "p_comment");
            printGetString(out, "Manufacturer#1", "p_mfgr");
            printGetString(out, "LG CASE", "p_container");
            out.println("</part>");

            out.println("<part>");
            printGetString(out, "2", "p_partkey");
            printGetString(out, "Brand#13", "p_brand");
            printGetString(out, "goldenrod lavender spring chocolate lace", "p_name");
            printGetString(out, "7", "p_size");
            printGetString(out, "PROMO BURNISHED COPPER", "p_type");
            printGetString(out, "901.00", "p_retailprice");
            printGetString(out, "ly. slyly ironi", "p_comment");
            printGetString(out, "Manufacturer#1", "p_mfgr");
            printGetString(out, "JUMBO PKG", "p_container");
            out.println("</part>");

            out.println("<part>");
            printGetString(out, "3", "p_partkey");
            printGetString(out, "Brand#42", "p_brand");
            printGetString(out, "spring green yellow purple cornsilk", "p_name");
            printGetString(out, "21", "p_size");
            printGetString(out, "STANDARD POLISHED BRASS", "p_type");
            printGetString(out, "903.00", "p_retailprice");
            printGetString(out, "egular deposits hag", "p_comment");
            printGetString(out, "Manufacturer#4", "p_mfgr");
            printGetString(out, "WRAP CASE", "p_container");
            out.println("</part>");

            out.println("<part>");
            printGetString(out, "4", "p_partkey");
            printGetString(out, "Brand#34", "p_brand");
            printGetString(out, "cornflower chocolate smoke green pink", "p_name");
            printGetString(out, "14", "p_size");
            printGetString(out, "SMALL PLATED BRASS", "p_type");
            printGetString(out, "904.00", "p_retailprice");
            printGetString(out, "p furiously r", "p_comment");
            printGetString(out, "Manufacturer#3", "p_mfgr");
            printGetString(out, "MED DRUM", "p_container");
            out.println("</part>");

            out.println("<part>");
            printGetString(out, "5", "p_partkey");
            printGetString(out, "Brand#34", "p_brand");
            printGetString(out, "forest brown coral puff cream", "p_name");
            printGetString(out, "15", "p_size");
            printGetString(out, "STANDARD POLISHED TIN", "p_type");
            printGetString(out, "905.00", "p_retailprice");
            printGetString(out, "wake carefully", "p_comment");
            printGetString(out, "Manufacturer#3", "p_mfgr");
            printGetString(out, "SM PKG", "p_container");
            out.println("</part>");

            out.println("<part>");
            printGetString(out, "6", "p_partkey");
            printGetString(out, "Brand#24", "p_brand");
            printGetString(out, "bisque cornflower lawn forest magenta", "p_name");
            printGetString(out, "4", "p_size");
            printGetString(out, "PROMO PLATED STEEL", "p_type");
            printGetString(out, "906.00", "p_retailprice");
            printGetString(out, "sual a", "p_comment");
            printGetString(out, "Manufacturer#2", "p_mfgr");
            printGetString(out, "MED BAG", "p_container");
            out.println("</part>");

            out.println("<part>");
            printGetString(out, "7", "p_partkey");
            printGetString(out, "Brand#11", "p_brand");
            printGetString(out, "moccasin green thistle khaki floral", "p_name");
            printGetString(out, "45", "p_size");
            printGetString(out, "SMALL PLATED COPPER", "p_type");
            printGetString(out, "907.00", "p_retailprice");
            printGetString(out, "lyly. ex", "p_comment");
            printGetString(out, "Manufacturer#1", "p_mfgr");
            printGetString(out, "SM BAG", "p_container");
            out.println("</part>");

            out.println("<part>");
            printGetString(out, "8", "p_partkey");
            printGetString(out, "Brand#44", "p_brand");
            printGetString(out, "misty lace thistle snow royal", "p_name");
            printGetString(out, "41", "p_size");
            printGetString(out, "PROMO BURNISHED TIN", "p_type");
            printGetString(out, "908.00", "p_retailprice");
            printGetString(out, "eposi", "p_comment");
            printGetString(out, "Manufacturer#4", "p_mfgr");
            printGetString(out, "LG DRUM", "p_container");
            out.println("</part>");

            out.println("<part>");
            printGetString(out, "9", "p_partkey");
            printGetString(out, "Brand#43", "p_brand");
            printGetString(out, "thistle dim navajo dark gainsboro", "p_name");
            printGetString(out, "12", "p_size");
            printGetString(out, "SMALL BURNISHED STEEL", "p_type");
            printGetString(out, "909.00", "p_retailprice");
            printGetString(out, "ironic foxe", "p_comment");
            printGetString(out, "Manufacturer#4", "p_mfgr");
            printGetString(out, "WRAP CASE", "p_container");
            out.println("</part>");

            out.println("<part>");
            printGetString(out, "10", "p_partkey");
            printGetString(out, "Brand#54", "p_brand");
            printGetString(out, "linen pink saddle puff powder", "p_name");
            printGetString(out, "44", "p_size");
            printGetString(out, "LARGE BURNISHED STEE", "p_type");
            printGetString(out, "910.01", "p_retailprice");
            printGetString(out, "ithely", "p_comment");
            printGetString(out, "Manufacturer#5", "p_mfgr");
            printGetString(out, "LG CAN", "p_container");
            out.println("</part>");

            out.println("<part>");
            printGetString(out, "11", "p_partkey");
            printGetString(out, "Brand#55", "p_brand");
            printGetString(out, "linen blue saddle puff powder", "p_name");
            printGetString(out, "44", "p_size");
            printGetString(out, "MEDIUm BURNISHED STEEL", "p_type");
            printGetString(out, "905.01", "p_retailprice");
            printGetString(out, "ithely final deposit", "p_comment");
            printGetString(out, "Manufacturer#5", "p_mfgr");
            printGetString(out, "LG CAN", "p_container");
            out.println("</part>");

            out.println("</m4s>");
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
