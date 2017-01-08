package com.esit;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;

import javax.naming.NamingException;
import javax.ws.rs.core.MultivaluedMap;

import org.json.JSONObject;

public class SaleManager {

    CustomerManager customer;
    ConnectionManager conn;
    private String programType;
    private String installationDateTime;
    private String notes;
    private String dateSigned;
    private String salesRepId;

    //default constructor, do nothing
    public SaleManager() {

    }

    //constructor
    public SaleManager(MultivaluedMap<String, String> formParams) {
        customer = new CustomerManager(
                formParams.get("fname").get(0),
                formParams.get("lname").get(0),
                formParams.get("email").get(0),
                formParams.get("homePhone").get(0),
                formParams.get("cellPhone").get(0),
                formParams.get("enbridge").get(0),
                formParams.get("address").get(0),
                formParams.get("unitNum").get(0),
                formParams.get("city").get(0),
                formParams.get("province").get(0),
                formParams.get("postalCode").get(0)
                );

        this.programType = formParams.get("programType").get(0);
        this.installationDateTime = formParams.get("installationDateTime").get(0);
        this.notes = formParams.get("notes").get(0);
        this.dateSigned = formParams.get("dateSigned").get(0);
        this.salesRepId = formParams.get("salesRepId").get(0);
    }

    public JSONObject getAllCompleted() throws NamingException {
        JSONObject jsonObject = new JSONObject();
        try {
            //create a query string
            String _query = "SELECT Sale.saleId, "
                    + "CONCAT(Customer.firstName, ' ', Customer.lastName) AS customerName, "
                    + "Program.programName, "
                    + "Address.street, "
                    + "Sale.installationDateTime, "
                    + "Sale.status "
                    + "FROM Sale "
                    + "JOIN Customer ON Sale.customer = Customer.customerId "
                    + "JOIN Program ON Sale.program = Program.programId "
                    + "JOIN Property ON Sale.customer = Property.customer "
                    + "JOIN Address ON Property.address = Address.addressId "
                    + "WHERE status = 'Completed'";

            //create a new Query object
            conn = new ConnectionManager();

            //execute the query statement and get the ResultSet
            ResultSet resultSet = conn.executeQuery(_query);

            //creating an object to keep a collection of JSONs
            Collection<JSONObject> sales = new ArrayList<JSONObject>();

            // Iterating through the Results and filling the jsonObject
            while (resultSet.next()) {
              //creating a temporary JSON object and put there a data from the database
              JSONObject tempJson = new JSONObject();
              tempJson.put("salesNumber", resultSet.getString("saleId"));
              tempJson.put("name", resultSet.getString("customerName"));
              tempJson.put("product", resultSet.getString("programName"));
              tempJson.put("address", resultSet.getString("street"));
              tempJson.put("installationDateTime", resultSet.getString("installationDateTime"));
              tempJson.put("status", resultSet.getString("status"));
              sales.add(tempJson);
            }

            //creating a final JSON object
            jsonObject.put("sales", sales);

          } catch (Exception e) {
              e.printStackTrace();
          } finally {
              //close the connection to the database
              conn.closeConnection();
          }
        return jsonObject;
    }

    public int create() {
        int result = 0;
        int customerID = 0;
        int salesRepId = 0;
        int programType = 0;
        int saleId = 0;
        try {

            //getting a connection to the Database
            conn = new ConnectionManager();

            //attempting to create a new customer
            customerID = customer.create(conn);

            //if returned id > 0 then everything went well
            if(customerID > 0) {

                //Customer's create closed a connection, so we are creating a new one
                conn = new ConnectionManager();

                //validating salesRepId
                String getSalesRepIdQuery = "SELECT employeeId "
                        + "FROM Employee "
                        + "WHERE employeeId = " + this.getSalesRepId() + " "
                        + "AND role = 'salesperson'";

                ResultSet resultSet = conn.executeQuery(getSalesRepIdQuery);
                if(resultSet.next()) {
                  salesRepId = Integer.parseInt(resultSet.getString("employeeId"));
                }

                //validating programType
                String getProgramTypeQuery = "SELECT programId "
                        + "FROM Program "
                        + "WHERE programId = " + this.getProgramType();

                resultSet = conn.executeQuery(getProgramTypeQuery);
                if(resultSet.next()) {
                  programType = Integer.parseInt(resultSet.getString("programId"));
                }

                //checking if we had valid salesRepId and programType before creating a new Sale
                if(salesRepId > 0 && programType > 0) {
                  //create new sale object
                  String newSaleQuery = "INSERT INTO Sale ("
                          + "customer, salesRepId, program, "
                          + "rentalAgreement, PADForm, dateSigned, "
                          + "installationDateTime, notes, status) "
                          + "VALUES(" + customerID + ", " + this.getSalesRepId() + ", " + this.getProgramType()
                          + ", NULL, NULL, '" + this.getDateSigned() + "', '"
                          + this.getInstallationDateTime() + "', '" + this.getNotes() + "', " + "'In progress')";

                  //execute new sale query
                  result = conn.executeUpdate(newSaleQuery);

                  //checking whether we created a Sale
                  String getSaleQuery = "SELECT saleId "
                          + "FROM Sale "
                          + "WHERE customer = " + customerID + " "
                          + "AND salesRepId = " + this.getSalesRepId() + " "
                          + "AND program = " + this.getProgramType() + " "
                          + "AND dateSigned = '" + this.getDateSigned() + "' "
                          + "AND installationDateTime = '" + this.getInstallationDateTime() + "' "
                          + "AND status = 'In progress'";

                  resultSet = conn.executeQuery(getSaleQuery);
                  if(resultSet.next()) {
                    saleId = Integer.parseInt(resultSet.getString("saleId"));
                  }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //close the connection to the database
            conn.closeConnection();
        }

        return saleId;
    }
    
    // Get all sales
    public JSONObject getAllSales() throws NamingException {
        JSONObject jsonObject = new JSONObject();
        try {
            //create a query string
            String _query = "SELECT Sale.saleId, " 
                    + "CONCAT(Customer.firstName, ' ', Customer.lastName) AS customerName, "
                    + "Program.programName, "
                    + "Address.street, "
                    + "Sale.dateSigned, "
                    + "Sale.status "
                    + "FROM Sale " 
                    + "JOIN Customer ON Sale.customer = Customer.customerId "
                    + "JOIN Program ON Sale.program = Program.programId "
                    + "JOIN Property ON Sale.customer = Property.customer "
                    + "JOIN Address ON Property.address = Address.addressId";
            
            //create a new Query object
            conn = new ConnectionManager();
            
            //execute the query statement and get the ResultSet
            ResultSet resultSet = conn.executeQuery(_query);

            //creating an object to keep a collection of JSONs
            Collection<JSONObject> sales = new ArrayList<JSONObject>();

            // Iterating through the Results and filling the jsonObject
            while (resultSet.next()) {
              //creating a temporary JSON object and put there a data from the database
              JSONObject tempJson = new JSONObject();
              tempJson.put("salesNumber", resultSet.getString("saleId"));
              tempJson.put("name", resultSet.getString("customerName"));
              tempJson.put("product", resultSet.getString("programName"));
              tempJson.put("address", resultSet.getString("street"));
              tempJson.put("date", resultSet.getString("dateSigned"));
              tempJson.put("status", resultSet.getString("status"));
              sales.add(tempJson);
            }

            //creating a final JSON object
            jsonObject.put("sales", sales);

          } catch (Exception e) {
              e.printStackTrace();
          } finally {
              //close the connection to the database
              conn.closeConnection();
          }
        return jsonObject;
    }

    // Get sale by Id
    public JSONObject getSaleById(int id) throws NamingException {
        JSONObject jsonObject = new JSONObject();
        try {
            //create a query string
            String _query = "SELECT Sale.saleId, " 
                    + "Customer.firstName, "
                    + "Customer.lastName, "
                    + "Program.programName, "
                    + "Address.street, "
                    + "Address.unit, "
                    + "Address.city, "
                    + "Address.province, "
                    + "Address.postalCode, "
                    + "Customer.enbridgeNum, "
                    + "Customer.email, "
                    + "Customer.homePhone, "
                    + "Customer.cellPhone, "
                    + "Program.programId, "
                    + "Sale.installationDateTime, "
                    + "Sale.notes, "
                    + "Sale.salesRepId "
                    + "FROM Sale " 
                    + "JOIN Customer ON Sale.customer = Customer.customerId "
                    + "JOIN Program ON Sale.program = Program.programId "
                    + "JOIN Property ON Sale.customer = Property.customer "
                    + "JOIN Address ON Property.address = Address.addressId "
                    + "WHERE Sale.saleId = " + id;
            
            //create a new Query object
            conn = new ConnectionManager();
            
            //execute the query statement and get the ResultSet
            ResultSet resultSet = conn.executeQuery(_query);
            
            //creating a temporary JSON object and put there a data from the database
            JSONObject sale = new JSONObject();

            // If there are results fill the jsonObject
            if (resultSet.next()) {
              sale.put("salesNumber", resultSet.getString("saleId"));
              sale.put("firstName", resultSet.getString("firstName"));
              sale.put("lastName", resultSet.getString("lastName"));
              sale.put("product", resultSet.getString("programName"));
              sale.put("address", resultSet.getString("street"));
              sale.put("unit", resultSet.getString("unit"));
              sale.put("city", resultSet.getString("city"));
              sale.put("province", resultSet.getString("province"));
              sale.put("postalCode", resultSet.getString("postalCode"));
              sale.put("enbridgeNum", resultSet.getString("enbridgeNum"));
              sale.put("homePhone", resultSet.getString("homePhone"));
              sale.put("cellPhone", resultSet.getString("cellPhone"));
              sale.put("email", resultSet.getString("email"));
              sale.put("programId", resultSet.getString("programId"));
              sale.put("installationDateTime", resultSet.getString("installationDateTime"));
              sale.put("notes", resultSet.getString("notes"));
              sale.put("salesRepId", resultSet.getString("salesRepId"));
            }
            
            //creating a final JSON object
            jsonObject.put("sale", sale);

          } catch (Exception e) {
              e.printStackTrace();
          } finally {
              //close the connection to the database
              conn.closeConnection();
          }
        return jsonObject;
    }


    public String getFname() {
        return customer.getFname();
    }
    public void setFname(String fname) {
        customer.setFname(fname);
    }
    public String getAddress() {
        return customer.getAddress();
    }
    public void setAddress(String address) {
        customer.setAddress(address);
    }
    public String getLname() {
        return customer.getLname();
    }
    public void setLname(String lname) {
        customer.setLname(lname);
    }
    public String getUnitNum() {
        return customer.getUnitNum();
    }
    public void setUnitNum(String unitNum) {
        customer.setUnitNum(unitNum);
    }
    public String getCity() {
        return customer.getCity();
    }
    public void setCity(String city) {
        customer.setCity(city);
    }
    public String getProvince() {
        return customer.getProvince();
    }
    public void setProvince(String province) {
        customer.setProvince(province);
    }
    public String getPostalCode() {
        return customer.getPostalCode();
    }
    public void setPostalCode(String postalCode) {
        customer.setPostalCode(postalCode);
    }
    public String getEnbridge() {
        return customer.getEnbridge();
    }
    public void setEnbridge(String enbridge) {
        customer.setEnbridge(enbridge);
    }
    public String getHomePhone() {
        return customer.getHomePhone();
    }
    public void setHomePhone(String homePhone) {
        customer.setHomePhone(homePhone);
    }
    public String getEmail() {
        return customer.getEmail();
    }
    public void setEmail(String email) {
        customer.setEmail(email);
    }
    public String getCellPhone() {
        return customer.getCellPhone();
    }
    public void setCellPhone(String cellPhone) {
        customer.setCellPhone(cellPhone);
    }
    public String getInstallationDateTime() {
        return installationDateTime;
    }
    public void setInstallationDateTime(String installationDateTime) {
        this.installationDateTime = installationDateTime;
    }
    public String getProgramType() {
        return programType;
    }
    public void setProgramType(String programType) {
        this.programType = programType;
    }

    public String getNotes() {
        return notes;
    }
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public String getDateSigned() {
        return dateSigned;
    }
    public void setDateSigned(String dateSigned) {
        this.dateSigned = dateSigned;
    }
    public String getSalesRepId() {
        return salesRepId;
    }
    public void setSalesRepId(String salesRepId) {
        this.salesRepId = salesRepId;
    }
}