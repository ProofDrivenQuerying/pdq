package uk.ac.ox.cs.pdq.ui;

import java.util.ArrayList;
import javafx.scene.control.TreeItem;

public class TreeViewHelper
{
    public TreeViewHelper()
    {
    }

    // This method creates an ArrayList of TreeItems ()
    public ArrayList<TreeItem> getProducts()
    {
        ArrayList<TreeItem> products = new ArrayList<TreeItem>();

        TreeItem cars = new TreeItem("Cars");
        cars.getChildren().addAll(getCars());


        products.add(cars);


        return products;
    }

    // This method creates an ArrayList of TreeItems ()
    private ArrayList<TreeItem> getCars()
    {
        ArrayList<TreeItem> cars = new ArrayList<TreeItem>();


    }

    private ArrayList<TreeItem> getModel(){
        ArrayList<TreeItem> models = new ArrayList<TreeItem>();

    }


}
